package com.github.wartman4404.glview.material;

import java.io.DataInputStream;
import java.io.IOException;

import com.github.wartman4404.glview.gl.GLHelper;
import com.github.wartman4404.glview.gl.ObjSaver;
import com.github.wartman4404.glview.gl.ShaderCompileException;
import com.github.wartman4404.glview.gl.Shape;

import android.opengl.GLES20;
import android.util.Log;

public abstract class PhongShadedMaterial implements GLMaterial {
	protected int mPositionHandle = -1;
	protected int mNormalHandle = -1;
	protected int mProgram = -1;
	protected int mSpecularHardnessHandle = -1;
	protected int mSpecularPowerHandle = -1;
	protected int mDiffusePowerHandle = -1;
	protected int mLightDirectionHandle = -1;
	protected int mDirectedLightHandle = -1;
	protected int mAmbientLightHandle = -1;
	
	protected String getCommonVertexShaderHeader() {
		return vertexShaderHeader;
	}
	protected String getCommonVertexShaderMain() {
		return vertexShaderMain;
	}
	
	protected String getCommonFragmentShaderHeader() {
		return fragmentShaderHeader;
	}

	protected String getCommonFragmentShaderMain() {
		return fragmentShaderMain;
	}
	
	protected abstract String getVertexShaderCode();
	protected abstract String getFragmentShaderCode();
	
    private final String vertexShaderHeader =
        "uniform mat4 uMVPMatrix;\n" +
        "uniform mat4 uMVMatrix;\n" +
        "uniform mat4 uNormalMatrix;\n" +
        "attribute vec4 mPosition;\n" +
        "attribute vec3 mNormal;\n" +
        "varying vec3 vNormal;\n" +
        "varying vec3 vPosition;\n";
    private final String vertexShaderMain =
        "void main() {\n" +
        "  gl_Position = uMVPMatrix * mPosition;\n" +
        "  vNormal = normalize(mat3(uNormalMatrix) * mNormal);\n" +
        "  vPosition = vec3(uMVMatrix * mPosition);\n" +
        "  saveColor();\n" +
        "}\n";

    private final String fragmentShaderHeader =
        "precision mediump float;\n" +
        "varying vec3 vNormal;\n" +
        "varying vec3 vPosition;\n" +
        "uniform float uSpecularHardness;\n" +
        "uniform float uSpecularPower;\n" +
        "uniform float uDiffusePower;\n" +
        "uniform vec3 uLightDir;\n" +
        "uniform vec3 uAmbientLight;\n" +
        "uniform vec3 uDirectedLight;\n";
    private final String fragmentShaderMain = 
        "void main() {\n" +
        "  vec4 color = getColor();\n" +
        "  vec3 lightDir = normalize(uLightDir);\n" +
        "  vec3 normalDir = normalize(vNormal);\n" +
        "  vec3 viewDirection = normalize(vPosition);\n" +
        "  float diffuseIntensity = max(dot(lightDir, normalDir), 0.0);\n" +
        "  diffuseIntensity = diffuseIntensity * 3.0;\n" +
        // test for light coming from right side goes here
        "  vec3 specularColor;\n" +
        "  if (dot(vNormal, uLightDir) < 0.0) {\n" +
        "    specularColor = vec3(0,0,0);\n" +
        "  } else {\n" +
        "    float specularIntensity = max(dot(reflect(-lightDir, normalDir), vec3(viewDirection)), 0.0);\n" + // add shininess exponent
        "    specularIntensity = pow(specularIntensity, uSpecularHardness);\n" +
        "    specularColor = specularIntensity * uSpecularPower * uDirectedLight;\n" + // multiply by light color - material color doesn't matter
        "  } " +
        "  vec3 diffuseColor = vec3(color) * (diffuseIntensity * uDiffusePower * uDirectedLight + uAmbientLight);\n" + // multiply material color by diffuse + ambient light colors
        "  gl_FragColor = vec4(diffuseColor + specularColor, 1);\n" +
        "}\n";

    public static int loadShader(int type, String shaderCode) {

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

    public void destroyProgram() {
    	if (!GLES20.glIsProgram(mProgram)) {
    		Log.i("glview", "program " + mProgram + " doesn't exist!");
    		return;
    	}
    	int[] ret = new int[1];
    	GLES20.glGetProgramiv(mProgram, GLES20.GL_ATTACHED_SHADERS, ret, 0);
    	int shaderCount = ret[0];
    	int[] shaders = new int[shaderCount];
    	GLES20.glGetAttachedShaders(mProgram, shaderCount, ret, 0, shaders, 0);
    	for (int i: shaders) {
    		GLES20.glDeleteShader(i);
    	}
    	GLES20.glDeleteProgram(mProgram);
    }
    
	@Override
	public int makeProgram() {
        // prepare shaders and OpenGL program
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER,
                                                   getVertexShaderCode());
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER,
                                                     getFragmentShaderCode());
        GLHelper.checkShaderLog("vertex log", GLES20.glGetShaderInfoLog(vertexShader), GLHelper.ERROR_VERTEX_SHADER);
        GLHelper.checkShaderLog("fragment log", GLES20.glGetShaderInfoLog(fragmentShader), GLHelper.ERROR_FRAGMENT_SHADER);

        int program = GLES20.glCreateProgram();             // create empty OpenGL Program
        GLES20.glAttachShader(program, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(program, fragmentShader); // add the fragment shader to program
        GLES20.glLinkProgram(program);                  // create OpenGL program executables
        GLHelper.checkShaderLog("program log", GLES20.glGetProgramInfoLog(program), GLHelper.ERROR_LINK_PROGRAM);
        GLHelper.glCheckErrorAndThrow();

        mPositionHandle = GLES20.glGetAttribLocation(program, "mPosition");
        mNormalHandle = GLES20.glGetAttribLocation(program, "mNormal");
        mSpecularHardnessHandle = GLES20.glGetUniformLocation(program, "uSpecularHardness");
        mSpecularPowerHandle = GLES20.glGetUniformLocation(program, "uSpecularPower");
        mDiffusePowerHandle = GLES20.glGetUniformLocation(program, "uDiffusePower");
        GLHelper.glCheckErrorAndThrow();

        mLightDirectionHandle = GLES20.glGetUniformLocation(program, "uLightDir");
        mDirectedLightHandle = GLES20.glGetUniformLocation(program, "uDirectedLight");
        mAmbientLightHandle = GLES20.glGetUniformLocation(program, "uAmbientLight");
        GLHelper.glCheckErrorAndThrow();

        GLHelper.logAttribNames(program);

        this.mProgram = program;
        return program;
	}

	public void prepareProgram(int baseOffset) {
        // Add program to OpenGL environment
        GLES20.glUseProgram(mProgram);
        
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glEnableVertexAttribArray(mNormalHandle);
	}
	
	public void finishProgram() {
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mNormalHandle);
	}
	
	public static final int COORD_STRIDE = 3 * 4;
	public static final int NORMAL_STRIDE = 3 * 4;
	
	public static final int COORD_OFFSET = 0;
	public static final int NORMAL_OFFSET = COORD_STRIDE;
	
	public static int getCoordStride() { return COORD_STRIDE; }
	public static int getNormalStride() { return NORMAL_STRIDE; }
	
	public static int getCoordOffset() { return COORD_OFFSET; }
	public static int getNormalOffset() { return NORMAL_OFFSET; }
	
	public abstract class PhongShadedInstance implements GLMaterialInstance {
		protected float mSpecularHardness;
		protected float mSpecularPower;
		protected float mDiffusePower;
		public PhongShadedInstance(PhongSave save) {
			mSpecularHardness = save.mSpecularHardness;
			// OBJ files specify an entire color, but that's more than is really called for
			mSpecularPower = save.mSpecularPower;
			mDiffusePower = save.mDiffusePower;
		}
		
		public void destroyInstance() { }

		public void loadMaterial(int baseOffset) {
			GLES20.glUniform1f(mSpecularHardnessHandle, mSpecularHardness);
			// OBJ files specify an entire color, but that's more than is really called for
			GLES20.glUniform1f(mSpecularPowerHandle, mSpecularPower);
			GLES20.glUniform1f(mDiffusePowerHandle, mDiffusePower);
		}
		
		public void loadLights(float[] lights) {
			GLES20.glUniform3fv(mLightDirectionHandle, 1, lights, Shape.LIGHT_DIRECTION_OFFSET);
			GLES20.glUniform3fv(mAmbientLightHandle, 1, lights, Shape.LIGHT_AMBIENT_COLOR_OFFSET);
			GLES20.glUniform3fv(mDirectedLightHandle, 1, lights, Shape.LIGHT_DIRECTED_COLOR_OFFSET);
		}
	
		public int getProgram() {
			return mProgram;
		}
		public void finishProgram() {
			PhongShadedMaterial.this.finishProgram();
		}
	}
}

abstract class PhongSave extends GLMaterialSave {
	protected float mSpecularHardness;
	protected float mSpecularPower;
	protected float mDiffusePower;
	protected String mName;

	public PhongSave(DataInputStream in) throws IOException {
		this.mName = ObjSaver.readString(in);
		this.mSpecularHardness = in.readFloat();
		this.mSpecularPower = in.readFloat();
		this.mDiffusePower = in.readFloat();
	}

	public String getName() {
		return mName;
	}
}
