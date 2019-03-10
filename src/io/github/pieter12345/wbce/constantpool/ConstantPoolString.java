package io.github.pieter12345.wbce.constantpool;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import io.github.pieter12345.wbce.ClassConstantPool;
import io.github.pieter12345.wbce.FancyByteArrayOutputStream;
import io.github.pieter12345.wbce.FancyInputStream;
import io.github.pieter12345.wbce.ClassConstantPool.ConstantPoolType;

public class ConstantPoolString implements ConstantPoolObject {
	
	// Variables & Constants.
	private byte[] strBytes;
	
	// Constructor.
	public ConstantPoolString(byte[] strBytes) {
		this.strBytes = strBytes;
	}
	public ConstantPoolString(String str) {
		this.strBytes = str.getBytes(StandardCharsets.UTF_8);
	}
	
	@Override
	public ConstantPoolType getType() {
		return ConstantPoolType.STRING;
	}
	
	public static ConstantPoolObject fromInputStream(FancyInputStream inStream) throws IOException {
		int strSize = inStream.readTwoByteInt();
		byte[] strBytes = new byte[strSize];
		inStream.read(strBytes, 0, strSize);
		return new ConstantPoolString(strBytes);
	}
	
	@Override
	public byte[] toBytes() {
		@SuppressWarnings("resource")
		FancyByteArrayOutputStream outStream = new FancyByteArrayOutputStream();
		outStream.write(this.getType().getTagByte());
		outStream.writeTwoByteInteger(this.strBytes.length);
		outStream.write(this.strBytes);
		return outStream.toByteArray();
	}
	
	public String getString() {
		return new String(this.strBytes, StandardCharsets.UTF_8);
	}
	
	public void setString(String str) {
		if(str.length() > 65535) {
			throw new RuntimeException("Max string size is 65535. Found string of size: " + str.length());
		}
		this.strBytes = str.getBytes(StandardCharsets.UTF_8);
	}
	
	public void setString(byte[] strBytes) {
		if(strBytes.length > 65535) {
			throw new RuntimeException("Max string size is 65535. Found string of size: " + strBytes.length);
		}
		this.strBytes = strBytes;
	}
	
	@Override
	public String val(ClassConstantPool constPool) {
		return new String(this.strBytes, StandardCharsets.UTF_8);
	}
	
	@Override
	public String simpleVal(ClassConstantPool constPool) {
		return this.val(constPool);
	}
}
