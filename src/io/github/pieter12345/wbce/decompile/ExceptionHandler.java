package io.github.pieter12345.wbce.decompile;

public class ExceptionHandler {
	
	// Variables & Constants.
	private int startPc;
	private int endPc;
	private int handlerPc;
	private String exceptionType;
	
	public ExceptionHandler(int startPc, int endPc, int handlerPc, String exceptionType) {
		this.startPc = startPc;
		this.endPc = endPc;
		this.handlerPc = handlerPc;
		this.exceptionType = exceptionType;
	}
	
	public int getStartPc() {
		return this.startPc;
	}
	
	public int getEndPc() {
		return this.endPc;
	}
	
	public int getHandlerPc() {
		return this.handlerPc;
	}
	
	public String getExceptionType() {
		return this.exceptionType;
	}
}
