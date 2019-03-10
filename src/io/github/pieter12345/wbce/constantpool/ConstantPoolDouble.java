package io.github.pieter12345.wbce.constantpool;

import java.io.IOException;

import io.github.pieter12345.wbce.ClassConstantPool;
import io.github.pieter12345.wbce.FancyByteArrayOutputStream;
import io.github.pieter12345.wbce.FancyInputStream;
import io.github.pieter12345.wbce.ClassConstantPool.ConstantPoolType;

public class ConstantPoolDouble implements ConstantPoolObject {
	
	// Variables & Constants.
	private double value;
	
	// Constructor.
	public ConstantPoolDouble(double value) {
		this.value = value;
	}
	
	@Override
	public ConstantPoolType getType() {
		return ConstantPoolType.DOUBLE;
	}
	
	public static ConstantPoolObject fromInputStream(FancyInputStream inStream) throws IOException {
		return new ConstantPoolDouble(inStream.readDouble());
	}
	
	@Override
	public byte[] toBytes() {
		@SuppressWarnings("resource")
		FancyByteArrayOutputStream outStream = new FancyByteArrayOutputStream();
		outStream.write(this.getType().getTagByte());
		outStream.writeDouble(this.value);
		return outStream.toByteArray();
	}
	
	public double getValue() {
		return this.value;
	}
	
	public void setValue(double value) {
		this.value = value;
	}
	
	@Override
	public String val(ClassConstantPool constPool) {
		return Double.toString(this.value);
	}
	
	@Override
	public String simpleVal(ClassConstantPool constPool) {
		return this.val(constPool);
	}
}
