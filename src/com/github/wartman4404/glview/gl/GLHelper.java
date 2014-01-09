package com.github.wartman4404.glview.gl;

import java.nio.charset.Charset;

import android.opengl.GLES20;
import android.util.Log;

public class GLHelper {
	public static final int ERROR_OTHER_SHADER = -1;
	public static final int ERROR_LINK_PROGRAM = 0;
	public static final int ERROR_VERTEX_SHADER = 1;
	public static final int ERROR_FRAGMENT_SHADER = 2;

	public static boolean glCheckError() {
		boolean wasEverError = false;
		while (true) {
			int error = GLES20.glGetError();
			if (error == GLES20.GL_NO_ERROR) {
				break;
			}
			wasEverError = true;
			Log.i("gl-error", getErrorName(error));
		}
		return wasEverError;
	}
	
	public static String getErrorName(int error) {
		switch (error) {
		case GLES20.GL_INVALID_ENUM:
			return "GL_INVALID_ENUM";
		case GLES20.GL_INVALID_VALUE:
			return "GL_INVALID_VALUE";
		case GLES20.GL_INVALID_OPERATION:
			return "GL_INVALID_OPERATION";
		case GLES20.GL_INVALID_FRAMEBUFFER_OPERATION:
			return "GL_INVALID_FRAMEBUFFER_OPERATION";
		case GLES20.GL_OUT_OF_MEMORY:
			return "GL_OUT_OF_MEMORY";
		default:
			return "unknown error 0x" + Integer.toHexString(error) + "!";
		}
	}

	public static boolean glCheckErrorAndThrow() {
		int error = GLES20.glGetError();
		if (error == GLES20.GL_NO_ERROR) {
			return false;
		} else {
			StringBuilder s = new StringBuilder(getErrorName(error));
			int allErrors = error;
			while (true) {
				error = GLES20.glGetError();
				if (error == GLES20.GL_NO_ERROR) break;
				if ((allErrors & error) == error) continue;
				allErrors |= error;
				s.append("|").append(getErrorName(error));
			}
			throw new android.opengl.GLException(allErrors, s.toString());
		}
	}

	public static String getAttribType(int type) {
		switch (type) {
		case GLES20.GL_FLOAT:
			return "GL_FLOAT";
		case GLES20.GL_FLOAT_VEC2:
			return "GL_FLOAT_VEC2";
		case GLES20.GL_FLOAT_VEC3:
			return "GL_FLOAT_VEC3";
		case GLES20.GL_FLOAT_VEC4:
			return "GL_FLOAT_VEC4";
	
		case GLES20.GL_INT:
			return "GL_INT";
		case GLES20.GL_INT_VEC2:
			return "GL_INT_VEC2";
		case GLES20.GL_INT_VEC3:
			return "GL_INT_VEC3";
		case GLES20.GL_INT_VEC4:
			return "GL_INT_VEC4";
	
		case GLES20.GL_BOOL:
			return "GL_BOOL";
		case GLES20.GL_BOOL_VEC2:
			return "GL_BOOL_VEC2";
		case GLES20.GL_BOOL_VEC3:
			return "GL_BOOL_VEC3";
		case GLES20.GL_BOOL_VEC4:
			return "GL_BOOL_VEC4";
		
		case GLES20.GL_SAMPLER_2D:
			return "GL_SAMPLER_2D";
		case GLES20.GL_SAMPLER_CUBE:
			return "GL_SAMPLER_CUBE";
	
		case GLES20.GL_FLOAT_MAT2:
			return "GL_FLOAT_MAT2";
		case GLES20.GL_FLOAT_MAT3:
			return "GL_FLOAT_MAT3";
		case GLES20.GL_FLOAT_MAT4:
			return "GL_FLOAT_MAT4";
		
		default:
			return "unknown type 0x"+Integer.toHexString(type) + "!";
		}
	}
	
	public static void logAttribNames(int program) {
        int[] result = new int[4];
        if (!GLES20.glIsProgram(program)) {
        	Log.e("glhelper", "No such program: " + program);
        	return;
        }
        GLES20.glGetProgramiv(program, GLES20.GL_ACTIVE_ATTRIBUTES, result, 0);
        GLHelper.glCheckErrorAndThrow();
        int count = result[0];
        Log.i("glstatus", "active attribs: " + count);
        if (count > 0) {
        	for (int i = 0; i < count; i++) {
        		AttribParameters params = getActiveAttrib(program, i);
        		Log.i("glstatus", "attrib "  + i + ": \"" + params.name + "\", type " + getAttribType(params.type) + ", size " + params.size);
        	}
        }
	}
	
	public static void logUniformNames(int program) {
        if (!GLES20.glIsProgram(program)) {
        	Log.e("glhelper", "No such program: " + program);
        	return;
        }
		int[] result = new int[4];
		GLES20.glGetProgramiv(program, GLES20.GL_ACTIVE_UNIFORMS, result, 0);
		GLHelper.glCheckErrorAndThrow();
		int count = result[0];
		Log.i("glstatus", "active uniforms: " + count);
		if (count > 0) {
			for (int i = 0; i < count; i++) {
				AttribParameters params = getActiveUniform(program, i);
        		Log.i("glstatus", "uniform "  + i + ": \"" + params.name + "\", type " + getAttribType(params.type) + ", size " + params.size);
			}
		}
	}
	
	public static AttribParameters getActiveAttrib(int program, int index) {
		int[] result = new int[3];
		GLES20.glGetProgramiv(program,  GLES20.GL_ACTIVE_ATTRIBUTE_MAX_LENGTH, result, 0);
		int maxLength = result[0];
		byte[] namearray = new byte[maxLength];

		GLES20.glGetActiveAttrib(program, index, maxLength, result, 0, result, 1, result, 2, namearray, 0);
    	int nameLength = result[0];
    	int attribSize = result[1];
    	int attribType = result[2];
	        	
    	String name = new String(namearray, 0, nameLength, Charset.forName("ascii"));
    	return new AttribParameters(name, attribSize, attribType);
	}
	
	public static AttribParameters getActiveUniform(int program, int index) {
		int[] result = new int[3];
		GLES20.glGetProgramiv(program, GLES20.GL_ACTIVE_UNIFORM_MAX_LENGTH, result, 0);
		int maxLength = result[0];
		byte[] namearray = new byte[maxLength];
		
		GLES20.glGetActiveUniform(program, index, maxLength, result, 0, result, 1, result, 2, namearray, 0);
    	int nameLength = result[0];
    	int attribSize = result[1];
    	int attribType = result[2];
	        	
    	String name = new String(namearray, 0, nameLength, Charset.forName("ascii"));
    	return new AttribParameters(name, attribSize, attribType);
	}

	public static class AttribParameters {
		String name;
		int size;
		int type;
		public AttribParameters(String name, int size, int type) {
			this.name = name;
			this.size = size;
			this.type = type;
		}
	}

	public static void checkShaderLog(String kind, String log, int code) {
		Log.i(kind, log);
		if (log.indexOf("error") != -1) {
			throw new ShaderCompileException(log, code);
		}
	}

	public static String matrixToString(float[] matrix) {
		StringBuilder b = new StringBuilder();
		b.append("[");
		for (int y = 0; y < 4; y++) {
			if (y > 0) {
				b.append(" ");
			}
			b.append("[");
			for (int x = 0; x < 4; x++) {
				String value = String.format("%6.2f", matrix[y*4+x]);
				b.append(value);
				if (x < 3) {
					b.append(",");
				}
			}
			b.append("]");
			if (y < 3) {
				b.append("\n");
			}
		}
		b.append("]");
		return b.toString();
	}
	
	public static boolean invertSubmatrix(float[] outmatrix, float[] matrix) {
		/*
		 *    [[a b c x]
		 *     [d e f x]
		 *     [g h i x]
		 *     [x x x x]]
		 */
		float a = matrix[0];
		float b = matrix[1];
		float c = matrix[2];
		float d = matrix[4];
		float e = matrix[5];
		float f = matrix[6];
		float g = matrix[8];
		float h = matrix[9];
		float i = matrix[10];

		float determinant = a*(e*i-f*h) - b*(i*d-f*g) + c*(d*h-e*g);
		if (determinant == 0.0f)
			return false;

		float invdet = 1/determinant;

		float A =  (e*i - f*h);
		float B = -(d*i - f*g);
		float C =  (d*h - e*g);
		float D = -(b*i - c*h);
		float E =  (a*i - c*g);
		float F = -(a*h - b*g);
		float G =  (b*f - c*e);
		float H = -(a*f - c*d);
		float I =  (a*e - b*d);

		outmatrix[ 0] = invdet * A; 
		outmatrix[ 4] = invdet * B;
		outmatrix[ 8] = invdet * C;
		outmatrix[ 1] = invdet * D; 
		outmatrix[ 5] = invdet * E;
		outmatrix[ 9] = invdet * F;
		outmatrix[ 2] = invdet * G; 
		outmatrix[ 6] = invdet * H;
		outmatrix[10] = invdet * I;

		return true;
	}
	
	
}
