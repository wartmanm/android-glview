package com.github.wartman4404.glview;

import com.github.wartman4404.glview.gl.ShaderCompileException;

import android.opengl.GLException;

public interface GLErrorListener {
	public void onGLException(GLException e);
	public void onCompileError(ShaderCompileException e);
}
