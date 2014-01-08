package com.github.wartman4404.glview.gl;

import android.opengl.GLES20;

public class Shape {
    public final int indexOffset;
    public final int indexCount;
    public static final int LIGHT_SIZE = 9;
    public static final int LIGHT_DIRECTION_OFFSET = 0;
    public static final int LIGHT_AMBIENT_COLOR_OFFSET = 3;
    public static final int LIGHT_DIRECTED_COLOR_OFFSET = 6;

    public Shape(int indexOffset, int indexCount) {
    	this.indexCount = indexCount;
    	this.indexOffset = indexOffset;
    }
    
    public void draw(int mProgram, float[] mMVPMatrix, float[] mMVMatrix, float[] mNormalMatrix) {
        int uMVMatrix = GLES20.glGetUniformLocation(mProgram, "uMVMatrix");
        if (uMVMatrix != -1)
        	GLES20.glUniformMatrix4fv(uMVMatrix, 1, false, mMVMatrix, 0);

        int uNormalMatrix = GLES20.glGetUniformLocation(mProgram, "uNormalMatrix");
        if (uNormalMatrix != -1)
        	GLES20.glUniformMatrix4fv(uNormalMatrix, 1, false, mNormalMatrix, 0);

        int uMVPMatrix = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        if (uMVPMatrix != -1)
        	GLES20.glUniformMatrix4fv(uMVPMatrix, 1, false, mMVPMatrix, 0);

        // Draw the triangle
		GLES20.glDrawElements(GLES20.GL_TRIANGLES, indexCount, GLES20.GL_UNSIGNED_SHORT, indexOffset);
		GLHelper.glCheckErrorAndThrow();
    }
    public int getIndexOffset() {
    	return indexOffset;
    }
    public int getIndexCount() {
    	return indexCount;
    }
}