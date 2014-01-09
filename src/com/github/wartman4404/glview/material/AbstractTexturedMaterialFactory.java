package com.github.wartman4404.glview.material;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.github.wartman4404.glview.gl.GLHelper;
import com.github.wartman4404.glview.gl.ObjSaver;
import com.github.wartman4404.glview.gl.ShaderCompileException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

public abstract class AbstractTexturedMaterialFactory extends PhongShadedMaterial {
	protected int mTextureHandle = -1;
    
	@Override
	public int makeProgram() {
		int program = super.makeProgram();
        mTextureHandle = GLES20.glGetAttribLocation(program, "mTexture");
        return program;
	}

	@Override
	public void prepareProgram(int baseOffset) {
		super.prepareProgram(baseOffset);
        GLES20.glEnableVertexAttribArray(mTextureHandle);
        
        // Prepare the triangle coordinate data
//        dataBuffer.position(baseOffset);
        GLES20.glVertexAttribPointer(mPositionHandle, 3,
                                     GLES20.GL_FLOAT, false,
                                     getFullStride(), baseOffset);
//		dataBuffer.position(baseOffset + NORMAL_OFFSET);
        GLES20.glVertexAttribPointer(mNormalHandle, 3,
                                     GLES20.GL_FLOAT, false,
                                     getFullStride(), baseOffset + NORMAL_OFFSET);
//		dataBuffer.position(baseOffset + TEXTURE_OFFSET);
        GLES20.glVertexAttribPointer(mTextureHandle, 2,
                                     GLES20.GL_FLOAT, false,
                                     getFullStride(), baseOffset + TEXTURE_OFFSET);
        GLHelper.glCheckErrorAndThrow();
	}
	
	public void finishProgram() {
		GLES20.glDisableVertexAttribArray(mTextureHandle);
        super.finishProgram();
	}
	
	protected Bitmap getBitmap(AbstractTextureSave save, MaterialLoader loader) {
		String filename = save.textureFilename;
        
        InputStream is = null;
        Bitmap bitmap = null;
        try {
        	is = loader.getMaterialStream(filename);
            bitmap = BitmapFactory.decodeStream(is);
        } catch (IOException e) {
        	Log.e("glview", "onsurfacechanged: failed loading texture bitmap " + filename);
        } finally {
            try {
            	if (is != null)
            		is.close();
            } catch(IOException e) {
            	Log.e("glview", "onsurfacechanged: failed closing texture bitmap" + filename);
            }
        }
        return bitmap;
	}
	
	@Override
	public AbstractTexturedMaterial createInstance(GLMaterialSave genericSave, MaterialLoader loader) {
		if (!(genericSave instanceof AbstractTextureSave)) {
			throw new IllegalArgumentException("got " + genericSave.getClass().getSimpleName() + " instead of " + AbstractTextureSave.class.getSimpleName());
		}
		AbstractTextureSave save = (AbstractTextureSave) genericSave;

		return null;
	}
	
	public class AbstractTexturedMaterial extends PhongShadedInstance {
		protected int mTextureID = -1;

		public AbstractTexturedMaterial(AbstractTextureSave save, Bitmap bitmap) {
			super(save);
			setTexture(bitmap);
		}

		public void setTexture(Bitmap texture) {
			if (texture == null) {
				throw new ShaderCompileException("Got null bitmap for texture");
			}
			int[] textures = new int[1];
			GLES20.glGenTextures(1, textures, 0);

			mTextureID = textures[0];
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureID);

			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
					GLES20.GL_NEAREST);
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
					GLES20.GL_TEXTURE_MAG_FILTER,
					GLES20.GL_LINEAR);

			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
					GLES20.GL_REPEAT);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
					GLES20.GL_REPEAT);

			GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, texture, 0);
			GLHelper.glCheckErrorAndThrow();
		}
		
		@Override
		public void destroyInstance() {
			if (GLES20.glIsTexture(mTextureID)) {
				GLES20.glDeleteTextures(1, new int[] { mTextureID }, 0);
			}
			super.destroyInstance();
		}

		@Override
		public void loadMaterial(int baseOffset) {
			// FIXME this probably shouldn't be here
			AbstractTexturedMaterialFactory.this.prepareProgram(baseOffset);
			super.loadMaterial(baseOffset);

			GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureID);
			GLHelper.glCheckErrorAndThrow();
		}
		public void finishProgram() {
			super.finishProgram();
			AbstractTexturedMaterialFactory.this.finishProgram();
		}
	}
	public static final int TEXTURE_STRIDE = 2 * 4;
	public static final int TEXTURE_OFFSET = NORMAL_OFFSET + NORMAL_STRIDE;
	public static final int FULL_STRIDE = COORD_STRIDE + NORMAL_STRIDE + TEXTURE_STRIDE;
	public static int getTextureStride() { return TEXTURE_STRIDE; }
	public static int getTextureOffset() { return TEXTURE_OFFSET; }

	public int getFullStride() {
		return COORD_STRIDE + NORMAL_STRIDE + TEXTURE_STRIDE;
	}
}
abstract class AbstractTextureSave extends PhongSave {
	protected String textureFilename;
	public AbstractTextureSave(DataInputStream in) throws IOException {
		super(in);
		textureFilename = ObjSaver.readString(in);
	}
}
