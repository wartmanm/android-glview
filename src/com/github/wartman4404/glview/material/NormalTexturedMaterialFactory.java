package com.github.wartman4404.glview.material;

import java.io.DataInputStream;
import java.io.IOException;

import com.github.wartman4404.glview.gl.GLHelper;
import com.github.wartman4404.glview.gl.ObjSaver;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.util.Log;

public class NormalTexturedMaterialFactory extends AbstractTexturedMaterialFactory {
	protected int mTangentHandle = -1;
	protected int mColorHandle = -1;
    
	@Override
	public int makeProgram() {
		int program = super.makeProgram();
        mTangentHandle = GLES20.glGetAttribLocation(program, "mTangent");
        mColorHandle = GLES20.glGetUniformLocation(program, "uColor");
        return program;
	}

	@Override
	public void prepareProgram(int baseOffset) {
		super.prepareProgram(baseOffset);
        GLES20.glEnableVertexAttribArray(mTangentHandle);
        
        // Prepare the triangle coordinate data
//        dataBuffer.position(baseOffset);
        GLES20.glVertexAttribPointer(mTangentHandle, 3,
                                     GLES20.GL_FLOAT, false,
                                     getFullStride(), baseOffset + TANGENT_OFFSET);
        GLHelper.glCheckErrorAndThrow();
	}
	
	public void finishProgram(int baseOffset) {
		GLES20.glDisableVertexAttribArray(mTangentHandle);
		super.finishProgram();
	}

    private final String extraVertexShaderCode =
        "attribute vec2 mTexture;\n" +
        "attribute vec3 mTangent;\n" +
        "varying vec2 vTexture;\n" +
        "varying vec3 vTangent;\n" +
        "void saveColor() {\n" +
        "  vTexture = vec2(mTexture.x, 1.0-mTexture.y);\n" +
        "  vTangent = normalize(mat3(uNormalMatrix) * mTangent);\n" +
        "}\n";

    private final String extraFragmentShaderCode =
        "varying vec2 vTexture;\n" +
        "varying vec3 vTangent;\n" +
        "uniform sampler2D sTexture;\n" +
        "uniform vec4 uColor;\n";
    
    private final String fragmentShaderMain = 
        "void main() {\n" +
        "  vec3 lightDir = normalize(uLightDir);\n" +
        "  vec3 normalDir = normalize(vNormal);\n" +
        "  vec3 viewDirection = normalize(vPosition);\n" +
        "  vec3 textureNormal = vec3(texture2D(sTexture, vTexture))*2.0 - vec3(1.0);\n" +
        "  vec3 bitangent = normalize(cross(normalDir, vTangent));\n" +
        "  normalDir = normalize(mat3(vTangent, bitangent, vNormal) * textureNormal);\n" +

        "  float diffuseIntensity = max(dot(lightDir, normalDir), 0.0);\n" +
        "  float specularIntensity = max(dot(reflect(-lightDir, normalDir), vec3(viewDirection)), 0.0);\n" + // add shininess exponent
        "  specularIntensity = pow(specularIntensity, uSpecularHardness);\n" +
        "  specularColor = specularIntensity * uSpecularPower * uDirectedLight;\n" + // multiply by light color - material color doesn't matter
        "  vec3 diffuseColor = vec3(uColor) * (diffuseIntensity * uDiffusePower * uDirectedLight + uAmbientLight);\n" + // multiply material color by diffuse + ambient light colors
        "  gl_FragColor = vec4(diffuseColor + specularColor, 1);\n" +
        "}\n";

	@Override
	protected String getVertexShaderCode() {
		return super.getCommonVertexShaderHeader() + extraVertexShaderCode + super.getCommonVertexShaderMain();
	}

	@Override
	protected String getFragmentShaderCode() {
		return super.getCommonFragmentShaderHeader() + extraFragmentShaderCode + fragmentShaderMain;
	}


	public static final int TANGENT_STRIDE = 3 * 4;
	public static final int TANGENT_OFFSET = TEXTURE_OFFSET + TEXTURE_STRIDE;
	public static final int FULL_STRIDE = COORD_STRIDE + NORMAL_STRIDE + TEXTURE_STRIDE + TANGENT_STRIDE;
	public static int getTangentStride() { return TANGENT_STRIDE; }
	public static int getTangentOffset() { return TANGENT_OFFSET; }

	public int getFullStride() {
		return COORD_STRIDE + NORMAL_STRIDE + TEXTURE_STRIDE + TANGENT_STRIDE;
	}
	
	public class NormalTexturedMaterial extends AbstractTexturedMaterial {
		float[] color;
		public NormalTexturedMaterial(NormalTexturedMaterialSave save, Bitmap bitmap) {
			super(save, bitmap);
			this.color = save.color;
		}
		
		@Override
		public void loadMaterial(int baseOffset) {
			super.loadMaterial(baseOffset);
			NormalTexturedMaterialFactory.this.prepareProgram(baseOffset);
			GLES20.glUniform4fv(mColorHandle, 1, color, 0);
			GLHelper.glCheckErrorAndThrow();
		}
	}

	@Override
	public NormalTexturedMaterial createInstance(GLMaterialSave genericSave, MaterialLoader loader) {
		if (!(genericSave instanceof NormalTexturedMaterialSave)) {
			throw new IllegalArgumentException("got " + genericSave.getClass().getSimpleName() + " instead of " + NormalTexturedMaterialSave.class.getSimpleName());
		}
		NormalTexturedMaterialSave save = (NormalTexturedMaterialSave) genericSave;
		Bitmap bitmap = this.getBitmap(save, loader);
		return new NormalTexturedMaterial(save, bitmap);
	}

	public static class NormalTexturedMaterialSave extends AbstractTextureSave {
		protected float[] color;
		public NormalTexturedMaterialSave(DataInputStream in) throws IOException {
			super(in);
			this.color = new float[4];
			for (int i = 0; i < color.length; i++) {
				this.color[i] = in.readFloat();
			}
		}

		@Override
		public int getId() {
			return ObjSaver.MATERIAL_NORMAL_TEXTURED_ID;
		}
	}
}
