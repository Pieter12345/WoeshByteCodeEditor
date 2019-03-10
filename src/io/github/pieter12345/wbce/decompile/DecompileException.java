package io.github.pieter12345.wbce.decompile;

@SuppressWarnings("serial")
public class DecompileException extends Exception {
	
	public DecompileException() {
		super();
	}
	
	public DecompileException(String message) {
		super(message);
	}
	
	public DecompileException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public DecompileException(Throwable cause) {
		super(cause);
	}
	
}
