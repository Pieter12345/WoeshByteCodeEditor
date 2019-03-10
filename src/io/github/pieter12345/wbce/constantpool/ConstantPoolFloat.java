package io.github.pieter12345.wbce.constantpool;

import java.io.IOException;

import io.github.pieter12345.wbce.ClassConstantPool;
import io.github.pieter12345.wbce.FancyByteArrayOutputStream;
import io.github.pieter12345.wbce.FancyInputStream;
import io.github.pieter12345.wbce.ClassConstantPool.ConstantPoolType;

public class ConstantPoolFloat implements ConstantPoolObject {
	
	// Variables & Constants.
	private float value;
	
	// Constructor.
	public ConstantPoolFloat(float value) {
		this.value = value;
	}
	
	@Override
	public ConstantPoolType getType() {
		return ConstantPoolType.FLOAT;
	}
	
	public static ConstantPoolObject fromInputStream(FancyInputStream inStream) throws IOException {
		return new ConstantPoolFloat(inStream.readFloat());
	}
	
	@Override
	public byte[] toBytes() {
		@SuppressWarnings("resource")
		FancyByteArrayOutputStream outStream = new FancyByteArrayOutputStream();
		outStream.write(this.getType().getTagByte());
		outStream.writeFloat(this.value);
		return outStream.toByteArray();
	}
	
	public float getValue() {
		return this.value;
	}
	
	public void setValue(float value) {
		this.value = value;
	}
	
	@Override
	public String val(ClassConstantPool constPool) {
		return Float.toString(this.value);
	}
	
	@Override
	public String simpleVal(ClassConstantPool constPool) {
		return this.val(constPool);
	}
}
