package com.github.wartman4404.glview.gl;

public class ShaderCompileException extends RuntimeException {
	public final Object tag;

	public ShaderCompileException(String message) {
		this(message, null);
	}
	
	public ShaderCompileException(String message, Throwable cause) {
		this(message, cause, null);
	}

	public ShaderCompileException(String message, Object tag) {
		super(message);
		this.tag = tag;
	}
	
	public ShaderCompileException(String message, Throwable cause, Object tag) {
		super(message, cause);
		this.tag = tag;
	}
}
