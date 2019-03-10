package io.github.pieter12345.wbce.constantpool;

import java.io.IOException;

import io.github.pieter12345.wbce.ClassConstantPool;
import io.github.pieter12345.wbce.FancyByteArrayOutputStream;
import io.github.pieter12345.wbce.FancyInputStream;
import io.github.pieter12345.wbce.ClassConstantPool.ConstantPoolType;

public class ConstantPoolMethodHandle implements ConstantPoolObject {
	
	// Variables & Constants.
	private byte refKind;
	private int refIndex;
	
	// Constructor.
	public ConstantPoolMethodHandle(byte refKind, int refIndex) {
		this.refKind = refKind;
		this.refIndex = refIndex;
	}
	
	@Override
	public ConstantPoolType getType() {
		return ConstantPoolType.METHOD_HANDLE;
	}
	
	public static ConstantPoolObject fromInputStream(FancyInputStream inStream) throws IOException {
		return new ConstantPoolMethodHandle(inStream.readByte(), inStream.readTwoByteInt());
	}
	
	@Override
	public byte[] toBytes() {
		@SuppressWarnings("resource")
		FancyByteArrayOutputStream outStream = new FancyByteArrayOutputStream();
		outStream.write(this.getType().getTagByte());
		outStream.write(this.refKind);
		outStream.writeTwoByteInteger(this.refIndex);
		return outStream.toByteArray();
	}
	
	public byte getRefKind() {
		return this.refKind;
	}
	
	public void setRefKind(byte refKind) {
		this.refKind = refKind;
	}
	
	public int getRefIndex() {
		return this.refIndex;
	}
	
	public void setRefIndex(int index) {
		this.refIndex = index;
	}
	
	@Override
	public String val(ClassConstantPool constPool) {
		return "Method handle: Reference kind: " + this.refKind + "Reference index: " + this.refIndex + "->"
				+ (constPool != null && constPool.size() >= this.refIndex ?
						constPool.get(this.refIndex).val(constPool) : "~UNKNOWN VALUE");
	}
	
	@Override
	public String simpleVal(ClassConstantPool constPool) {
		return this.refKind + " " + (constPool != null && constPool.size() >= this.refIndex ?
				constPool.get(this.refIndex).simpleVal(constPool) : "~UNKNOWN_VALUE");
	}
}
