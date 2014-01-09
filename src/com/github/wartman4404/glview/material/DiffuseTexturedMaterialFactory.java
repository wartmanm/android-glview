package com.github.wartman4404.glview.material;

import java.io.DataInputStream;
import java.io.IOException;

import com.github.wartman4404.glview.gl.ObjSaver;

import android.graphics.Bitmap;

public class DiffuseTexturedMaterialFactory extends AbstractTexturedMaterialFactory {
	protected int mTextureHandle = -1;

    private final String extraVertexShaderCode =
        "attribute vec2 mTexture;\n" +
        "varying vec2 vTexture;\n" +
        "void saveColor() {\n" +
        "  vTexture = vec2(mTexture.x, 1.0-mTexture.y);\n" +
        "}\n";

    private final String extraFragmentShaderCode =
        "varying vec2 vTexture;\n" +
        "uniform sampler2D sTexture;\n" +
        "vec4 getColor() {\n" +
        "  return texture2D(sTexture, vTexture);\n" +
        "}\n";
    

	@Override
	protected String getVertexShaderCode() {
		return super.getCommonVertexShaderHeader() + extraVertexShaderCode + super.getCommonVertexShaderMain();
	}

	@Override
	protected String getFragmentShaderCode() {
		return super.getCommonFragmentShaderHeader() + extraFragmentShaderCode + super.getCommonFragmentShaderMain();
	}
	
	public class DiffuseTexturedMaterial extends AbstractTexturedMaterial {
		public DiffuseTexturedMaterial(DiffuseTexturedMaterialSave save, Bitmap bitmap) {
			super(save, bitmap);
		}
	}

	@Override
	public DiffuseTexturedMaterial createInstance(GLMaterialSave genericSave, MaterialLoader loader) {
		if (!(genericSave instanceof DiffuseTexturedMaterialSave)) {
			throw new IllegalArgumentException("got " + genericSave.getClass().getSimpleName() + " instead of " + DiffuseTexturedMaterialSave.class.getSimpleName());
		}
		DiffuseTexturedMaterialSave save = (DiffuseTexturedMaterialSave) genericSave;
		Bitmap bitmap = this.getBitmap(save, loader);
		return new DiffuseTexturedMaterial(save, bitmap);
	}
	public static class DiffuseTexturedMaterialSave extends AbstractTextureSave {
		public DiffuseTexturedMaterialSave(DataInputStream in) throws IOException {
			super(in);
		}

		@Override
		public int getId() {
			return ObjSaver.MATERIAL_DIFFUSE_TEXTURED_ID;
		}
	}
}
