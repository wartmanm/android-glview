package com.github.wartman4404.glview.animation;

import com.github.wartman4404.glview.time.TimeSource;

import android.animation.TimeInterpolator;
import android.opengl.Matrix;

public class AnimationChain extends GLAnimation {
	GLAnimation[] animations;
	float[] offsets;
	float[] proportions;
	TimeInterpolator[] interpolators;
	
	public AnimationChain(GLAnimation[] animations, float[] proportions, TimeInterpolator[] interpolators) {
		this.animations = animations;
		this.offsets = new float[proportions.length];
		this.proportions = proportions;
		this.interpolators = interpolators;
		float offset = 0;
		for (int i = 0; i < proportions.length; i++) {
			this.offsets[i] = offset;
			offset += proportions[i];
		}
	}

	@Override
	public void loadMatrix(float[] matrix, float completion) {
		throw new UnsupportedOperationException();
	}
	
	public class AnimateOnceInstance extends GLAnimation.AnimateOnceInstance {
		float[] lastAnimMatrix;
		float[] tempMatrix;
		int index;
		public AnimateOnceInstance(TimeSource timeSource, int duration, TimeInterpolator interpolator) {
			super(timeSource, duration, interpolator);
			this.lastAnimMatrix = new float[16];
			this.tempMatrix = new float[16];
			Matrix.setIdentityM(lastAnimMatrix, 0);
			index = 0;
		}
		@Override
		public void loadMatrix(long time, float[] modelMatrix) {
			float completion = getElapsedProportion(timeSource.elapsed(time));
			if (index >= animations.length) {
				System.arraycopy(lastAnimMatrix, 0, modelMatrix, 0, 16);
				return;
			}
			float localCompletion = (completion - offsets[index]) / proportions[index];
			float interpolatedCompletion = interpolators[index].getInterpolation(localCompletion);
			animations[index].loadMatrix(tempMatrix, interpolatedCompletion);
			Matrix.multiplyMM(modelMatrix, 0, tempMatrix, 0, lastAnimMatrix, 0);
			if (localCompletion >= 1f) {
				System.arraycopy(modelMatrix, 0, lastAnimMatrix, 0, 16);
				index++;
			}
		} 
	}
}
