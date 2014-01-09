package com.github.wartman4404.glview.animation;

import com.github.wartman4404.glview.time.TimeSource;

import android.animation.TimeInterpolator;

public abstract class GLAnimation {
	
	public static interface AnimateInstance {
		public void loadMatrix(long time, float[] modelMatrix);
		public boolean isEnded(long time);
	}

	public abstract void loadMatrix(float[] matrix, float completion);
	
	abstract class AbstractAnimateInstance implements AnimateInstance {
		protected TimeSource timeSource;
		public int duration;
		public AbstractAnimateInstance(TimeSource timeSource, int duration) {
			this.timeSource = timeSource;
			this.duration = duration;
		}
		public void loadMatrix(long time, float[] modelMatrix) {
			GLAnimation.this.loadMatrix(modelMatrix, getElapsedProportion(timeSource.elapsed(time)));
		}
		public abstract float getElapsedProportion(long elapsed);
		public abstract boolean isEnded(long time);
	}

	public class AnimateOnceInstance extends AbstractAnimateInstance {
		public TimeInterpolator interpolator;
		public AnimateOnceInstance(TimeSource timeSource, int duration, TimeInterpolator interpolator) {
			super(timeSource, duration);
			this.interpolator = interpolator;
		}
		public float getElapsedProportion(long elapsed) {
			float elapsedPos = ((float)elapsed) / duration;
			if (elapsedPos < 0) elapsedPos = 0;
			if (elapsedPos > 1) elapsedPos = 1;
			float pos = interpolator.getInterpolation(elapsedPos);
			return pos;
		}
		public boolean isEnded(long time) {
			return timeSource.elapsed(time) > duration;
		}
	}


	public class AnimateOngoingInstance extends AbstractAnimateInstance {
		public AnimateOngoingInstance(TimeSource timeSource, int duration) {
			super(timeSource, duration);
		}
		public float getElapsedProportion(long elapsed) {
			return ((float)(elapsed % duration)) / duration;
		}
		public boolean isEnded(long time) {
			return false;
		}

	}
}