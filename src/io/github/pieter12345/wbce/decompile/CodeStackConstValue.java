package io.github.pieter12345.wbce.decompile;

public class CodeStackConstValue extends CodeStackObject {
	
	private final String strVal;
	private final Object value;
	
	public CodeStackConstValue(int instructionIndex, byte value) {
		super(instructionIndex, "byte");
		this.strVal = "" + value;
		this.value = value;
	}
	public CodeStackConstValue(int instructionIndex, short value) {
		super(instructionIndex, "short");
		this.strVal = "" + value;
		this.value = value;
	}
	public CodeStackConstValue(int instructionIndex, int value) {
		super(instructionIndex, "int");
		this.strVal = "" + value;
		this.value = value;
	}
	public CodeStackConstValue(int instructionIndex, long value) {
		super(instructionIndex, "long");
		this.strVal = "" + value;
		this.value = value;
	}
	public CodeStackConstValue(int instructionIndex, float value) {
		super(instructionIndex, "float");
		this.strVal = "" + value;
		this.value = value;
	}
	public CodeStackConstValue(int instructionIndex, double value) {
		super(instructionIndex, "double");
		this.strVal = "" + value;
		this.value = value;
	}
	public CodeStackConstValue(int instructionIndex, char value) {
		super(instructionIndex, "char");
		this.strVal = "" + value;
		this.value = value;
	}
	public CodeStackConstValue(int instructionIndex, String value) {
		super(instructionIndex, "String");
		this.strVal = value;
		this.value = value;
	}
	
	@Override
	public String getDecompStr() {
		return this.strVal;
	}

	public Object getValue() {
		return this.value;
	}
	
//	public enum ValueType {
//		BYTE,
//		SHORT,
//		INT,
//		LONG,
//		FLOAT,
//		DOUBLE,
//		CHAR,
//		LOCAL_VAR,
//		OBJECT_REF;
//	}
}
