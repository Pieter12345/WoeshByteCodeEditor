package io.github.pieter12345.wbce.constantpool;

import java.io.IOException;

import io.github.pieter12345.wbce.ClassConstantPool;
import io.github.pieter12345.wbce.FancyByteArrayOutputStream;
import io.github.pieter12345.wbce.FancyInputStream;
import io.github.pieter12345.wbce.ClassConstantPool.ConstantPoolType;

public class ConstantPoolInteger implements ConstantPoolObject {
	
	// Variables & Constants.
	private int value;
	
	// Constructor.
	public ConstantPoolInteger(int value) {
		this.value = value;
	}
	
	@Override
	public ConstantPoolType getType() {
		return ConstantPoolType.INTEGER;
	}
	
	public static ConstantPoolObject fromInputStream(FancyInputStream inStream) throws IOException {
		return new ConstantPoolInteger(inStream.readFourByteInt());
	}
	
	@Override
	public byte[] toBytes() {
		@SuppressWarnings("resource")
		FancyByteArrayOutputStream outStream = new FancyByteArrayOutputStream();
		outStream.write(this.getType().getTagByte());
		outStream.writeFourByteInteger(this.value);
		return outStream.toByteArray();
	}
	
	public int getValue() {
		return this.value;
	}
	
	public void setValue(int value) {
		this.value = value;
	}
	
	@Override
	public String val(ClassConstantPool constPool) {
		return Integer.toString(this.value);
	}
	
	@Override
	public String simpleVal(ClassConstantPool constPool) {
		return this.val(constPool);
	}
}
