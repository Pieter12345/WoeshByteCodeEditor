package io.github.pieter12345.wbce.decompile;

public class CodeStackCode extends CodeStackObject {
	
	private final String codeStr;
	
	public CodeStackCode(int instructionIndex, String codeStr, String returnType) {
		super(instructionIndex, returnType);
		this.codeStr = codeStr;
	}
	
	@Override
	public String getDecompStr() {
		return this.codeStr;
	}
	
}
