package com.github.wartman4404.glview.gl;

import android.opengl.Matrix;

import com.github.wartman4404.glview.animation.AnimationStack;
import com.github.wartman4404.glview.animation.GLAnimation.AnimateInstance;
import com.github.wartman4404.glview.material.MaterialLoader;

public class GLElementGroup {
	private final AnimationStack activeAnimation; // for now this is enough
	private float[] modelMatrix;
	private float[] MVPMatrix;
	private float[] MVMatrix;
	private float[] normalMatrix;
	GLElement[] elements;
	BoundingBox bbox;
	public boolean isVisible;
	public GLElementGroup(GLElement[] elements, BoundingBox bbox) {
		this.elements = elements;
		this.bbox = bbox;
		this.isVisible = true;
		this.activeAnimation = new AnimationStack();
		this.modelMatrix = new float[16];
		this.MVPMatrix = new float[16];
		this.MVMatrix = new float[16];
		this.normalMatrix = new float[16];
		resetMatrix();
	}
	public void loadMaterial(MaterialLoader loader) {
		for (GLElement e: elements) {
			e.loadMaterial(loader);
		}
	}
	public void setMatrix(float[] matrix) {
		for (int i = 0; i < modelMatrix.length; i++) modelMatrix[i] = matrix[i];
	}
	public void resetMatrix() {
		Matrix.setIdentityM(modelMatrix, 0);
		Matrix.setIdentityM(normalMatrix, 0);
	}
	public void addAnimation(AnimateInstance anim, int pos) {
		this.activeAnimation.addAnimation(anim, pos);
	}
	public void draw(long time, float[] VMatrix, float[] PMatrix, float[] lights) {
		if (!isVisible) {
			return;
		}
        if (activeAnimation != null) {
        	activeAnimation.loadMatrix(time, modelMatrix);
        }
        Matrix.multiplyMM(MVMatrix, 0, VMatrix, 0, modelMatrix, 0);
        Matrix.multiplyMM(MVPMatrix, 0, PMatrix, 0, MVMatrix, 0);
        
        // TODO the matrices only need to be loaded once per material kind?
        for (GLElement e: elements) {
        	e.draw(MVPMatrix, MVMatrix, MVMatrix, lights);
        }
	}
	public GLElement[] getElements() {
		return elements;
	}
	public int getAnimationSlot() {
		return activeAnimation.getNextAnimationSlot();
	}
	public BoundingBox getBoundingBox() {
		return new BoundingBox(bbox);
	}
	public void unloadElementMaterials() {
		for (GLElement e: elements) {
			e.unloadMaterialInstance();
		}
	}
}