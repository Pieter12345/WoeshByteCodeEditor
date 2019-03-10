package io.github.pieter12345.wbce.decompile;

public abstract class CodeStackObject {
	
	// Variables & Constants.
	private final int instructionIndex;
	private final String returnType;
	
	public CodeStackObject(int instructionIndex, String returnType) {
		if(returnType == null) {
			throw new NullPointerException("Return type may not be null.");
		}
		this.instructionIndex = instructionIndex;
		this.returnType = returnType;
	}
	
	public int getInstructionIndex() {
		return this.instructionIndex;
	}
	
	public String getReturnType() {
		return this.returnType;
	}
	
	public abstract String getDecompStr();
}
