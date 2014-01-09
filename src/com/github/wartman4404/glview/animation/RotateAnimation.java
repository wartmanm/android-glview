package com.github.wartman4404.glview.animation;


import android.opengl.Matrix;

public class RotateAnimation extends GLAnimation {
	float[] tempMatrix;
	float xAxis;
	float yAxis;
	float zAxis;
	float xScale;
	float yScale;
	float zScale;
	float startAngle;
	float addAngle;
	public RotateAnimation(float xAxis, float yAxis, float zAxis, float xScale, float yScale, float zScale, float fromAngle, float toAngle) {
		tempMatrix = new float[32];
		this.xAxis = xAxis;
		this.yAxis = yAxis;
		this.zAxis = zAxis;
		this.xScale = xScale;
		this.yScale = yScale;
		this.zScale = zScale;
		this.startAngle = fromAngle;
		this.addAngle = toAngle - fromAngle;
	}

	@Override
	public void loadMatrix(float[] matrix, float completion) {
		Matrix.setIdentityM(tempMatrix, 0);
		Matrix.translateM(tempMatrix, 0, xAxis, yAxis, zAxis);
		Matrix.setRotateM(tempMatrix, 16, startAngle + completion * addAngle, xScale, yScale, zScale);
		Matrix.multiplyMM(matrix, 0, tempMatrix, 0, tempMatrix, 16);
		Matrix.translateM(matrix, 0, -xAxis, -yAxis, -zAxis);
	}
	
	public static void setTranslateM(float[] matrix, float x, float y, float z) {
		Matrix.setIdentityM(matrix, 0);
		matrix[3] = x;
		matrix[7] = y;
		matrix[11] = z;
	}

}
