package io.github.pieter12345.wbce.decompile;

public class CodeStackNull extends CodeStackObject {
	
	public CodeStackNull(int instructionIndex) {
		super(instructionIndex, "null");
	}
	
	@Override
	public String getDecompStr() {
		return "null";
	}
	
}
