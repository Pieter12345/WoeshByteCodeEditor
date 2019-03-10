package io.github.pieter12345.wbce.constantpool;

import java.io.IOException;

import io.github.pieter12345.wbce.ClassConstantPool;
import io.github.pieter12345.wbce.FancyByteArrayOutputStream;
import io.github.pieter12345.wbce.FancyInputStream;
import io.github.pieter12345.wbce.ClassConstantPool.ConstantPoolType;

public class ConstantPoolLong implements ConstantPoolObject {
	
	// Variables & Constants.
	private long value;
	
	// Constructor.
	public ConstantPoolLong(long value) {
		this.value = value;
	}
	
	@Override
	public ConstantPoolType getType() {
		return ConstantPoolType.LONG;
	}
	
	public static ConstantPoolObject fromInputStream(FancyInputStream inStream) throws IOException {
		return new ConstantPoolLong(inStream.readLong());
	}
	
	@Override
	public byte[] toBytes() {
		@SuppressWarnings("resource")
		FancyByteArrayOutputStream outStream = new FancyByteArrayOutputStream();
		outStream.write(this.getType().getTagByte());
		outStream.writeLong(this.value);
		return outStream.toByteArray();
	}
	
	public long getValue() {
		return this.value;
	}
	
	public void setValue(long value) {
		this.value = value;
	}
	
	@Override
	public String val(ClassConstantPool constPool) {
		return Long.toString(this.value);
	}
	
	@Override
	public String simpleVal(ClassConstantPool constPool) {
		return this.val(constPool);
	}
}
