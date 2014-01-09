package com.github.wartman4404.glview.gl;

import android.util.Log;

import com.github.wartman4404.glview.material.GLMaterial;
import com.github.wartman4404.glview.material.GLMaterialSave;
import com.github.wartman4404.glview.material.MaterialLoader;
import com.github.wartman4404.glview.material.GLMaterial.GLMaterialInstance;

public class GLElement {
	private final Shape shape;
	private final GLMaterial factory;
	private final GLMaterialSave materialProps;
	private int baseOffset;
	private GLMaterialInstance material = null;
	public GLElement(Shape shape, GLMaterial factory, GLMaterialSave materialProps, int baseOffset) {
		this.shape = shape;
		this.materialProps = materialProps;
		this.baseOffset = baseOffset;
		this.factory = factory;
	}
	public void loadMaterial(MaterialLoader loader) {
		if (this.material != null) {
			this.material.destroyInstance();
		}
		this.material = factory.createInstance(materialProps, loader);
	}
	public void draw(float[] MVPMatrix, float[] MVMatrix, float[] NormalMatrix,  float[] lights) {
        if (material == null) {
        	Log.w(String.format("glelement %h", this), "material is still null!!");
        } else {
        	material.loadMaterial(baseOffset);
        	material.loadLights(lights);
        	shape.draw(material.getProgram(), MVPMatrix, MVMatrix, NormalMatrix);
        	material.finishProgram();
        }
	}
	public GLMaterialSave getMaterial() {
		return materialProps;
	}
	public int getVertexOffset() {
		return baseOffset;
	}
	public int getIndexOffset() {
		return shape.getIndexOffset();
	}
	public int getIndexCount() {
		return shape.getIndexCount();
	}
	public GLMaterial getBaseMaterial() {
		return factory;
	}
	public GLElement replaceMaterial(GLMaterial factory, GLMaterialSave props) {
		return new GLElement(shape, factory, props, baseOffset);
	}
	public void unloadMaterialInstance() {
		if (material != null) {
			material.destroyInstance();
		}
	}
}