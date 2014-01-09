package com.github.wartman4404.glview.animation;


import android.opengl.Matrix;

public class TranslateAnimation extends GLAnimation {
	float x;
	float y;
	float z;
	
	public TranslateAnimation(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	@Override
	public void loadMatrix(float[] matrix, float completion) {
		Matrix.setIdentityM(matrix, 0);
		Matrix.translateM(matrix, 0, x*completion, y*completion, z*completion);
	}

}
