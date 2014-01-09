package com.github.wartman4404.glview;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


import com.github.wartman4404.glview.animation.GLAnimation.AnimateInstance;
import com.github.wartman4404.glview.gl.BoundingBox;
import com.github.wartman4404.glview.gl.GLElementGroup;
import com.github.wartman4404.glview.gl.GLHelper;
import com.github.wartman4404.glview.gl.ObjSaver;
import com.github.wartman4404.glview.gl.ObjSaver.MaterialFactories;
import com.github.wartman4404.glview.gl.ShaderCompileException;
import com.github.wartman4404.glview.gl.Shape;
import com.github.wartman4404.glview.gl.ObjSaver.VBOData;
import com.github.wartman4404.glview.material.GLMaterial;
import com.github.wartman4404.glview.material.MaterialLoader;

import android.opengl.GLES20;
import android.opengl.GLException;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

public class GLRenderer implements GLSurfaceView.Renderer {
	
    private float[] mProjMatrix = new float[16];
    private float[] mVMatrix = new float[16];
    private float[] mLights = new float[Shape.LIGHT_SIZE];

    protected Map<String, GLElementGroup> mGLElements;
    private MaterialFactories factories;
    
    private int mVertBufHandle;
    private int mIdxBufHandle;
    
    private ByteBuffer vertBuf;
    private ByteBuffer idxBuf;
    
    private MaterialLoader loader;
    
    private BoundingBox bbox;

	private GLErrorListener mErrorListener;
	
	public static final int NOT_PENDING = 0;
	public static final int PENDING_LOAD = 1;
	public static final int PENDING_UNLOAD = 2;
	private int pendingState; // queue load or unload for gl thread
	private boolean loaded; // don't do anything, including unload, when this is unset
	
	private Queue<Runnable> glRunnables = new ArrayDeque<Runnable>();
    
    public GLRenderer(InputStream materialIn, InputStream elementIn, MaterialLoader loader, MaterialFactories factories) throws IOException {
    	this.loader = loader;
    	this.factories = factories;
    	loadFile(materialIn, elementIn);
    }
    
    public void loadFile(InputStream materialIn, InputStream elementIn) throws IOException {
    	if (loaded) {
    		Log.e("glview", "glrenderer: called loadFile while still loaded!");
    		return;
    	}
    	if (pendingState == PENDING_UNLOAD) {
    		Log.e("glview", "glrenderer: canceling pending unload!");
    	}
    	VBOData data = ObjSaver.loadFile(materialIn, elementIn, factories);
    	Log.i("glview", "glrenderer: loaded file");
    	mGLElements = data.elements;
    	vertBuf = data.vertBuf;
    	idxBuf = data.idxBuf;
    	loadBoundingBox();
    	recenterView();
    	pendingState = PENDING_LOAD;
    }
    
    protected void loadBoundingBox() {
    	BoundingBox newbox = null;
    	for (GLElementGroup e: mGLElements.values()) {
    		if (newbox == null) {
    			newbox = new BoundingBox(e.getBoundingBox());
    		} else {
    			newbox.addBox(e.getBoundingBox());
    		}
    	}
    	newbox.squarify();
    	bbox = newbox;
    }
    
    private void recenterView() {
        float x = bbox.centerX();
        float y = bbox.centerY();
        Matrix.setLookAtM(mVMatrix, 0, x, y, bbox.minZ - 2, x, y, 0f, 0f, 1.0f, 0.0f);
    }
    
    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
    	try {
    		Log.i("glview", "glrenderer: onsurfacecreated");
    		// Set the background frame color
    		GLES20.glEnable(GLES20.GL_DEPTH_TEST);
    		GLES20.glClearDepthf(1);
    		
    		createBuffers();
    		createMaterials();

        } catch (GLException e) {
        	Log.i("glview", "glrenderer: Got GL exception: " + e.getMessage());
        	Log.i("glview", e.getStackTrace()[1].toString());
        	if (mErrorListener != null) {
        		mErrorListener.onGLException(e);
        	}
        } catch (ShaderCompileException e) {
        	if (mErrorListener != null) {
        		mErrorListener.onCompileError(e);
        	}
    	}
    }
    
    public void createMaterialInstances() throws GLException, ShaderCompileException {
    	for (String s: mGLElements.keySet()) {
    		mGLElements.get(s).loadMaterial(loader);
    	}
    }
    
    public void createMaterials() throws GLException, ShaderCompileException {
    	for (GLMaterial material: factories.getLoadedMaterials()) {
    		if (material != null) {
    			material.makeProgram();
    		}
    	}
    }
    
    public void createBuffers() throws GLException {
    	int[] buffers = new int[2];

    	GLES20.glGenBuffers(2, buffers, 0);
    	GLHelper.glCheckErrorAndThrow();
    	int vertBufHandle = buffers[0];
    	int idxBufHandle = buffers[1];
    	this.mVertBufHandle = vertBufHandle;
    	this.mIdxBufHandle = idxBufHandle;
    	GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertBufHandle);
    	GLHelper.glCheckErrorAndThrow();
    	GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, idxBufHandle);
    	GLHelper.glCheckErrorAndThrow();
    	GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertBuf.limit(), vertBuf, GLES20.GL_STATIC_DRAW);
    	GLHelper.glCheckErrorAndThrow();
    	GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, idxBuf.limit(), idxBuf, GLES20.GL_STATIC_DRAW);
    	GLHelper.glCheckErrorAndThrow();

    	// free bytebuffers, we won't need them again
    	// WHOOPS YES WE WILL
    	//    	this.vertBuf = null;
    	//    	this.idxBuf = null;
    }
    
    private void gl_load() {
    	try {
    		createBuffers();
    		createMaterials();
    		createMaterialInstances();
    		loaded = true;
    		pendingState = NOT_PENDING;
    	} catch (GLException e) {
    		Log.i("glview", "glrenderer: Got GL exception: " + e.getMessage());
    		Log.i("glview", e.getStackTrace()[1].toString());
    		if (mErrorListener != null) {
    			mErrorListener.onGLException(e);
    		}
    	} catch (ShaderCompileException e) {
    		if (mErrorListener != null) {
    			mErrorListener.onCompileError(e);
    		}
    	}
    }

    @Override
    public void onDrawFrame(GL10 unused) {
    	if (pendingState == PENDING_UNLOAD) {
    		gl_unload();
    	} else if (pendingState == PENDING_LOAD) {
    		gl_load();
    	}
    	try {
    		runCallbacks();
    		if (!loaded) {
    			return;
    		}


    		if (!checkBuffers()) {
    			Log.i("glview", "glrenderer: bailing because no buffers!");
    			mErrorListener.onGLException(new GLException(-1, "you ain't got no buffers, son!"));
    			return;
    		}
    		// Draw background color
    		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

    		long time = System.currentTimeMillis();

    		// Draw triangle
    		for (String s: mGLElements.keySet()) {
    			mGLElements.get(s).draw(time, mVMatrix, mProjMatrix, mLights);
    		}
        } catch (GLException e) {
        	Log.i("glview", "glrenderer: Got GL exception: " + e.getMessage());
        	Log.i("glview", e.getStackTrace()[1].toString());
        	if (mErrorListener != null) {
        		mErrorListener.onGLException(e);
        	}
        } catch (ShaderCompileException e) {
        	if (mErrorListener != null) {
        		mErrorListener.onCompileError(e);
        	}
        }
    }
    
    private boolean checkBuffers() {
    	int[] result = new int[2];
    	boolean success = true;
    	success &= checkBuffer(GLES20.GL_ARRAY_BUFFER_BINDING, GLES20.GL_ARRAY_BUFFER, result, "GL_ARRAY_BUFFER");
    	success &= checkBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER_BINDING, GLES20.GL_ELEMENT_ARRAY_BUFFER, result, "GL_ELEMENT_ARRAY_BUFFER");
    	return success;
    }
    
    private boolean checkBuffer(int binding, int buffer, int[] result, String name) {
    	GLES20.glGetIntegerv(binding, result, 0);
    	if (result[0] == 0) {
    		Log.i("glview", "buffers: buffer " + name + " doesn't exist!");
    		return false;
    	} else {
    		GLES20.glGetBufferParameteriv(buffer, GLES20.GL_BUFFER_SIZE, result, 1);
    		return result[1] > 0;
    	}
    }
    
    public void addAnimation(String s, AnimateInstance anim, int position) {
    	GLElementGroup e = mGLElements.get(s);
    	if (e != null) {
    		setAnimation(e, anim, position);
    	}
    }
    
    private void setAnimation(GLElementGroup e, AnimateInstance anim, int position) {
    	e.addAnimation(anim, position);
    }
    
    public void addAnimation(String s, AnimateInstance anim) {
    	GLElementGroup e = mGLElements.get(s);
    	if (e != null) {
    		e.addAnimation(anim, e.getAnimationSlot());
    	}
    }
    
    public void setLightDirection(float[] in, int offset) {
    	this.mLights[Shape.LIGHT_DIRECTION_OFFSET+0] = in[offset+0];
    	this.mLights[Shape.LIGHT_DIRECTION_OFFSET+1] = in[offset+1];
    	this.mLights[Shape.LIGHT_DIRECTION_OFFSET+2] = in[offset+2];
    }
    
    public void setAmbientLight(float intensity) {
    	this.mLights[Shape.LIGHT_AMBIENT_COLOR_OFFSET+0] = intensity;
    	this.mLights[Shape.LIGHT_AMBIENT_COLOR_OFFSET+1] = intensity;
    	this.mLights[Shape.LIGHT_AMBIENT_COLOR_OFFSET+2] = intensity;
    }

    public void setDirectedLight(float intensity) {
    	this.mLights[Shape.LIGHT_DIRECTED_COLOR_OFFSET+0] = intensity;
    	this.mLights[Shape.LIGHT_DIRECTED_COLOR_OFFSET+1] = intensity;
    	this.mLights[Shape.LIGHT_DIRECTED_COLOR_OFFSET+2] = intensity;
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
    	Log.i("glview", "glrenderer: onsurfacechanged");
    	GLES20.glClearColor(0f, 0f, 0f, 0f);
        // Adjust the viewport based on geometry changes,
        // such as screen rotation
        GLES20.glViewport(0, 0, width, height);
        float screenRatio = (float) width / height;
        float xratio = bbox.width() / (2*screenRatio);
        float yratio = bbox.height() / 2;
        float ratio = xratio > yratio ? xratio : yratio;
        float screenx = screenRatio*ratio;
        float screeny = ratio;
        Log.i("glview", "glrenderer: onsurfacechanged: screenratio is " + screenRatio + ", xratio is " + xratio + ", yratio is " + yratio);
        Log.i("glview", String.format("glrenderer: setting width to %.3f, %.3f, height to %.3f, %.3f", -screenx, screenx, -screeny, screeny));
        
        Matrix.frustumM(mProjMatrix, 0, -screenx, screenx, -screeny, screeny, 1.9f, bbox.depth()+2f);

        Log.i("glview", "onsurfacechanged: loading materials");

        try {
        	createMaterialInstances();
        } catch (GLException e) {
        	Log.i("glview", "glrenderer: Got GL exception: " + e.getMessage());
        	Log.i("glview", e.getStackTrace()[1].toString());
        	if (mErrorListener != null) {
        		mErrorListener.onGLException(e);
        	}
        } catch (ShaderCompileException e) {
        	if (mErrorListener != null) {
        		mErrorListener.onCompileError(e);
        	}
        }
    }
    public boolean getVisibility(String name) {
    	return mGLElements.get(name).isVisible;
    }
    public void setVisibility(String name, boolean visible) {
    	mGLElements.get(name).isVisible = visible;
    }
    
    public GLElementGroup setElement(String name, GLElementGroup elem) {
    	return mGLElements.put(name, elem);
    }
    public GLElementGroup getElement(String name) {
    	return mGLElements.get(name);
    }
    public String[] getElementNames() {
    	String[] array = new String[mGLElements.size()];
    	return mGLElements.keySet().toArray(array);
    }
    public BoundingBox getBounds() {
    	return bbox;
    }
    
    public void unload(Runnable callback) {
    	if (!loaded) {
    		Log.i("glview", "glrenderer: called unload while already unloaded!");
    		return;
    	}
    	pendingState = PENDING_UNLOAD;
    	if (callback != null) {
    		runOnGLThread(callback);
    	}
    }
    
    public void runOnGLThread(Runnable runnable) {
    	glRunnables.add(runnable);
    }
    
    private void runCallbacks() {
    	int size = glRunnables.size();
    	if (size > 0) {
    		Log.i("glview", "glrenderer: running " + size + " callbacks");
    	}
    	// if more runnables are added, wait until next time to process them
    	// this is probably what the caller wants, otherwise why not
    	// do the work in the existing callback?
    	while (size > 0) {
    		glRunnables.remove().run();
    		size--;
    	}
    }
    
    private void gl_unload() {
    	try {
    		for (String s: mGLElements.keySet()) {
    			GLElementGroup eg = mGLElements.get(s);
    			eg.unloadElementMaterials();
    		}
    		for (GLMaterial m: factories.getLoadedMaterials()) {
    			m.destroyProgram();
    		}
    		GLHelper.glCheckErrorAndThrow();
    		unloadBuffer(mVertBufHandle);
    		unloadBuffer(mIdxBufHandle);
    		GLHelper.glCheckErrorAndThrow();
    	} catch (RuntimeException e) {
    		Log.e("glview", "glrenderer: Error while unloading gl resources");
    		throw(e);
    	}
    	loaded = false;
    	pendingState = NOT_PENDING;
    }
    
    private void unloadBuffer(int bufferHandle) {
    	if (GLES20.glIsBuffer(bufferHandle)) {
    		GLES20.glDeleteBuffers(1, new int[] { bufferHandle }, 0);
    	}
    }

    public void setErrorListener(GLErrorListener listener) {
    	this.mErrorListener = listener;
    }
    
    public boolean isLoaded() {
    	return this.loaded;
    }
}
