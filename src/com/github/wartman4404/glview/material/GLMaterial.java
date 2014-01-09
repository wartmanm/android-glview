package com.github.wartman4404.glview.material;

public interface GLMaterial {
	public int makeProgram();
	public void prepareProgram(int baseOffset);
	public GLMaterialInstance createInstance(GLMaterialSave save, MaterialLoader loader);
	public int getFullStride();
	public void destroyProgram();

	public interface GLMaterialInstance {
		public void loadMaterial(int baseOffset);
		public void loadLights(float[] lights);
		public int getProgram();
		public void finishProgram();
		public void destroyInstance();
	}
}