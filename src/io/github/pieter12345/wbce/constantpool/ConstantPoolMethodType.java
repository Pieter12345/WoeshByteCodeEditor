package io.github.pieter12345.wbce.constantpool;

import java.io.IOException;

import io.github.pieter12345.wbce.ClassConstantPool;
import io.github.pieter12345.wbce.FancyByteArrayOutputStream;
import io.github.pieter12345.wbce.FancyInputStream;
import io.github.pieter12345.wbce.ClassConstantPool.ConstantPoolType;

public class ConstantPoolMethodType implements ConstantPoolObject {
	
	// Variables & Constants.
	private int methodTypeIndex;
	
	// Constructor.
	public ConstantPoolMethodType(int index) {
		this.methodTypeIndex = index;
	}
	
	@Override
	public ConstantPoolType getType() {
		return ConstantPoolType.METHOD_TYPE;
	}
	
	public static ConstantPoolObject fromInputStream(FancyInputStream inStream) throws IOException {
		return new ConstantPoolMethodType(inStream.readTwoByteInt());
	}
	
	@Override
	public byte[] toBytes() {
		@SuppressWarnings("resource")
		FancyByteArrayOutputStream outStream = new FancyByteArrayOutputStream();
		outStream.write(this.getType().getTagByte());
		outStream.writeTwoByteInteger(this.methodTypeIndex);
		return outStream.toByteArray();
	}
	
	public int getIndex() {
		return this.methodTypeIndex;
	}
	
	public void setIndex(int index) {
		this.methodTypeIndex = index;
	}
	
	@Override
	public String val(ClassConstantPool constPool) {
		return "Method type index: " + this.methodTypeIndex + "->"
				+ (constPool != null && constPool.size() >= this.methodTypeIndex ?
						constPool.get(this.methodTypeIndex).val(constPool) : "~UNKNOWN VALUE");
	}
	
	@Override
	public String simpleVal(ClassConstantPool constPool) {
		return (constPool != null && constPool.size() >= this.methodTypeIndex ?
				constPool.get(this.methodTypeIndex).simpleVal(constPool) : "~UNKNOWN_VALUE");
	}
}
