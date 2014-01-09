package com.github.wartman4404.glview;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import com.github.wartman4404.glview.gl.ObjSaver;
import com.github.wartman4404.glview.gl.ObjSaver.MaterialFactories;
import com.github.wartman4404.glview.material.MaterialLoader;


import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;

public class GLObjectView extends GLSurfaceView {
	
	public static final String schema = "http://schemas.android.com/apk/lib/com.github.wartman4404.glview";
	private float[] lightDir;
	private float lightAmbient;
	private float lightDirectional;
	private String materialSource;
	private String vertexSource;
	private MaterialFactories materialFactories;
	private MaterialLoader materialLoader;
	private GLRenderer mRenderer;

    public GLObjectView(Context context) {
        super(context);
        init();
    }

	public GLObjectView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
		loadAttrs(context, attrs);
	}
	
	private void init() {
		lightDir = new float[3];
		materialFactories = new ObjSaver.DefaultMaterialFactories();
		materialLoader = new AssetMaterialLoader(getContext());
	}

	public GLRenderer getRenderer() {
		return mRenderer;
	}
	
	public String getMaterialSource() {
		return materialSource;
	}

	public void setMaterialSource(String materialSource) {
		this.materialSource = materialSource;
	}

	public String getVertexSource() {
		return vertexSource;
	}

	public void setVertexSource(String vertexSource) {
		this.vertexSource = vertexSource;
	}
	
	public MaterialFactories getMaterialFactories() {
		return this.materialFactories;
	}

	public void setMaterialFactories(MaterialFactories factories) {
		this.materialFactories = factories;
	}
	
	public MaterialLoader getMaterialLoader() {
		return materialLoader;
	}
	
	public void setMaterialLoader(MaterialLoader loader) {
		this.materialLoader = loader;
	}
    
    public void getLightDir(float[] out, int offset) {
    	out[0+offset] = lightDir[0];
    	out[1+offset] = lightDir[1];
    	out[2+offset] = lightDir[2];
    }
    
    public void setLightDir(float[] in, int offset) {
    	lightDir[0] = in[0+offset];
    	lightDir[1] = in[1+offset];
    	lightDir[2] = in[2+offset];
    	updateLight();
    }
    
    public float getLightAmbientIntensity() {
		return lightAmbient;
	}

	public void setLightAmbientIntensity(float lightAmbient) {
		this.lightAmbient = lightAmbient;
		updateLight();
	}

	public float getLightDirectionalIntensity() {
		return lightDirectional;
	}

	public void setLightDirectionalIntensity(float lightDirectional) {
		this.lightDirectional = lightDirectional;
		updateLight();
	}

	private void updateLight() {
		if (mRenderer != null) {
			mRenderer.setLightDirection(lightDir, 0);
			mRenderer.setAmbientLight(lightAmbient);
			mRenderer.setDirectedLight(lightDirectional);
		}
    }
    
    private void loadAttrs(Context context, AttributeSet attrs) {
        TypedArray attributesArray = context.obtainStyledAttributes(attrs, R.styleable.GLObjectView, 0, 0);
        materialSource = attributesArray.getString(R.styleable.GLObjectView_src_material);
        vertexSource = attributesArray.getString(R.styleable.GLObjectView_src_object);
        lightDir[0] = attributesArray.getFloat(R.styleable.GLObjectView_light_dir_x, 0);
        lightDir[1] = attributesArray.getFloat(R.styleable.GLObjectView_light_dir_y, 0);
        lightDir[2] = attributesArray.getFloat(R.styleable.GLObjectView_light_dir_z, 0);
        lightAmbient = attributesArray.getFloat(R.styleable.GLObjectView_light_ambient_intensity, 0);
        lightDirectional = attributesArray.getFloat(R.styleable.GLObjectView_light_directional_intensity, 0);
        
        attributesArray.recycle();

    	Log.i("glstopwatch", "got material filename: " + materialSource + ", object filename: " + vertexSource);
    }
    
    public void reload(final Runnable callback) {
    	final GLRenderer renderer = getRenderer();
    	if (renderer == null) {
    		Log.i("globjectview", "creating new renderer");
    		createRenderer(this.getContext(), materialSource, vertexSource);
    		if (callback != null) {
    			getRenderer().runOnGLThread(callback);
    		}
    	} else if (!renderer.isLoaded()) {
    		load();
    		if (callback != null) {
    			getRenderer().runOnGLThread(callback);
    		}
    	} else {
        // is this necessary?
    		final int oldMode = getRenderMode();
    		setRenderMode(RENDERMODE_WHEN_DIRTY);
    		getRenderer().unload(new Runnable() {
    			public void run() {
    				loadRenderer(getContext(), materialSource, vertexSource);
    				setRenderMode(oldMode);
    				if (callback != null) {
    					getRenderer().runOnGLThread(callback);
    				}
    			}
    		});
    		requestRender();
    	}
    }
    
    public void unload() {
    	GLRenderer renderer = getRenderer();
    	if (renderer != null && getRenderer().isLoaded()) {
    		getRenderer().unload(null);
    	}
    }
    
    public void load() {
    	GLRenderer renderer = getRenderer();
    	if (renderer == null) {
    		createRenderer(getContext(), materialSource, vertexSource);
    	} else if (!renderer.isLoaded()) {
    		loadRenderer(getContext(), materialSource, vertexSource);
    	}
    }
    
    protected void loadRenderer(Context context, String materialSource, String vertexSource) {
    	try {
        	InputStream materialIn = materialLoader.getMaterialStream(materialSource);
        	InputStream vertexIn = materialLoader.getMaterialStream(vertexSource);
    		getRenderer().loadFile(materialIn, vertexIn);
    		materialIn.close();
    		vertexIn.close();
    	} catch (IOException e) {
    		Log.i("globjectview", "failed to reload files :(");
    	}
    }
    
    protected void createRenderer(final Context context, String materialSource, String vertexSource) {
    	if (this.isInEditMode() || materialSource == null || vertexSource == null) {
    		return;
    	}

        // Create an OpenGL ES 2.0 context.
    	// Because the GL version is part of the EGL config, this call must come before
    	// setEGLConfigChooser.
    	// This does not appear to be specified anywhere in the Android docs.
        setEGLContextClientVersion(2);
        
        // request a config with transparency
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        
        // Set the Renderer for drawing on the GLSurfaceView
        InputStream materialIn, vertexIn;
        try {
        	materialIn = materialLoader.getMaterialStream(materialSource);
        	vertexIn = materialLoader.getMaterialStream(vertexSource);
        } catch (IOException e) {
        	return;
        }
        try {
        	GLRenderer renderer = new GLRenderer(materialIn, vertexIn, materialLoader, materialFactories);
        	materialIn.close();
        	vertexIn.close();
        	this.setRenderer(renderer);
        	this.mRenderer = renderer;
        } catch (IOException e) {
        	Log.e("globjectview", "IO exception while loading renderer :(");
        	e.printStackTrace();
        }
        // ask for transparency from surface holder
        this.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        // ask to be drawn after everything else
        this.setZOrderOnTop(true);
        
        updateLight();
    }
    
    public static class AssetMaterialLoader implements MaterialLoader {
    	private final Context context;
    	
    	public AssetMaterialLoader(Context context) {
    		this.context = context;
    	}

		public InputStream getMaterialStream(String name) throws FileNotFoundException, IOException {
			return tryOpenStream(name, context);
		}

    	private InputStream tryOpenStream(String name, Context context) throws FileNotFoundException, IOException {
    		try {
    			if (name.startsWith("/")) {
    				Log.i("globjectview", "opening file " + name);
    				return new BufferedInputStream(new FileInputStream(name));
    			} else {
    				Log.i("globjectview", "opening asset " + name);
    				return new BufferedInputStream(context.getAssets().open(name));
    			}
    		} catch (IOException e) {
    			Log.e("globjectview", "failed to open \"" + name + "\" :(");
    			throw e;
    		}
    	}
    }
}
