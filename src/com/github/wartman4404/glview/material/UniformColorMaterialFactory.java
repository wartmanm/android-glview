package com.github.wartman4404.glview.material;

import java.io.DataInputStream;
import java.io.IOException;

import com.github.wartman4404.glview.gl.GLHelper;
import com.github.wartman4404.glview.gl.ObjSaver;


import android.opengl.GLES20;

public class UniformColorMaterialFactory extends PhongShadedMaterial {
	
	protected int mColorHandle = -1;

    private final String extraVertexShaderCode =
        "void saveColor() { }\n";

    private final String extraFragmentShaderCode =
        "uniform vec4 uColor;\n" +
        "vec4 getColor() {\n" +
        "  return uColor;\n" +
        "}\n";
    
	@Override
	public int makeProgram() {
		int program = super.makeProgram();
        mColorHandle = GLES20.glGetUniformLocation(program, "uColor");
        return program;
	}

	@Override
	public void prepareProgram(int baseOffset) {
		super.prepareProgram(baseOffset);
        
        // Prepare the triangle coordinate data
//        dataBuffer.position(baseOffset);
        GLES20.glVertexAttribPointer(mPositionHandle, 3,
                                     GLES20.GL_FLOAT, false,
                                     getFullStride(), baseOffset);
//		dataBuffer.position(baseOffset + NORMAL_OFFSET);
        GLES20.glVertexAttribPointer(mNormalHandle, 3,
                                     GLES20.GL_FLOAT, false,
                                     getFullStride(), baseOffset + NORMAL_OFFSET);
        GLHelper.glCheckErrorAndThrow();
	}

	@Override
	protected String getVertexShaderCode() {
		return super.getCommonVertexShaderHeader() + extraVertexShaderCode + super.getCommonVertexShaderMain();
	}

	@Override
	protected String getFragmentShaderCode() {
		return super.getCommonFragmentShaderHeader() + extraFragmentShaderCode + super.getCommonFragmentShaderMain();
	}

	@Override
	public GLMaterialInstance createInstance(GLMaterialSave save, MaterialLoader loader) {
		if (!(save instanceof UniformMaterialSave)) {
			throw new IllegalArgumentException("got " + save.getClass().getSimpleName() + " instead of " + UniformMaterialSave.class.getSimpleName());
		}
		return new UniformColorMaterial((UniformMaterialSave) save);
	}
	
	class UniformColorMaterial extends PhongShadedInstance {
		protected float[] color;

		public UniformColorMaterial(UniformMaterialSave save) {
			super(save);
			this.color = save.color;
		}

		@Override
		public void loadMaterial(int baseOffset) {
			UniformColorMaterialFactory.this.prepareProgram(baseOffset);
			super.loadMaterial(baseOffset);

			GLES20.glUniform1f(mDiffusePowerHandle, 1f);
			GLES20.glUniform4fv(mColorHandle, 1, color, 0);
			GLHelper.glCheckErrorAndThrow();
		}
	}
	public static final int FULL_STRIDE = COORD_STRIDE + NORMAL_STRIDE;
	@Override
	public int getFullStride() {
		return FULL_STRIDE;
	}

	public static class UniformMaterialSave extends PhongSave {
		protected float[] color;
		public UniformMaterialSave(DataInputStream in) throws IOException {
			super(in);
			this.color = new float[4];
			for (int i = 0; i < color.length; i++) {
				this.color[i] = in.readFloat();
			}
		}

		@Override
		public int getId() {
			return ObjSaver.MATERIAL_UNIFORM_ID;
		}
	}
}