package io.github.pieter12345.wbce.decompile;

public class CodeStackVariable extends CodeStackObject {
	
	private final String varName;
	
	public CodeStackVariable(int instructionIndex, String varName, String varType) {
		super(instructionIndex, varType);
		this.varName = varName;
	}
	
	public String getVarName() {
		return this.varName;
	}
	
	@Override
	public String getDecompStr() {
		return this.varName;
	}
	
}
