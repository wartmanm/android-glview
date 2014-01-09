package com.github.wartman4404.glview.animation;

import java.util.NavigableMap;
import java.util.TreeMap;

import android.opengl.Matrix;
import android.util.Log;

import com.github.wartman4404.glview.animation.GLAnimation.AnimateInstance;
public class AnimationStack implements AnimateInstance {
	public NavigableMap<Integer, AnimateInstance> anims;
	public float[] tempMatrix;

	public void addAnimation(AnimateInstance anim, int order) {
		anims.put(order, anim);
	}
	public AnimationStack() {
		anims = new TreeMap<Integer, AnimateInstance>();
		tempMatrix = new float[32];
	}

	@Override
	public boolean isEnded(long time) {
		return anims.isEmpty();
	}
	@Override
	public void loadMatrix(long time, float[] modelMatrix) {
		Matrix.setIdentityM(modelMatrix, 0);
		if (anims.isEmpty()) {
			return;
		}
		int i = anims.firstKey();
		int max = anims.lastKey();
		while(true) {
			AnimateInstance instance = anims.get(i);
			if (instance.isEnded(time)) {
				anims.remove(i);
			} else {
				anims.get(i).loadMatrix(time, tempMatrix);
				Matrix.multiplyMM(tempMatrix, 16, tempMatrix, 0, modelMatrix, 0);
				System.arraycopy(tempMatrix, 16, modelMatrix, 0, 16);
			}
			if (i == max)
				break;
			i = anims.higherKey(i);
		}
	}
	
	private String getAnimationType(AnimateInstance instance) {
		try {
			GLAnimation container = (GLAnimation) instance.getClass().getDeclaredField("this$0").get(instance);
			return container.getClass().getName();
		} catch (Exception e) {
			return instance.getClass().getName();
		}

	}
	
	public int getNextAnimationSlot() {
		if (anims.isEmpty()) {
			return 0;
		} else {
			return anims.lastKey() + 1;
		}
	}
}
