package io.github.pieter12345.wbce.constantpool;

import java.io.IOException;

import io.github.pieter12345.wbce.ClassConstantPool;
import io.github.pieter12345.wbce.FancyByteArrayOutputStream;
import io.github.pieter12345.wbce.FancyInputStream;
import io.github.pieter12345.wbce.ClassConstantPool.ConstantPoolType;

public class ConstantPoolStringRef implements ConstantPoolObject {
	
	// Variables & Constants.
	private int index;
	
	// Constructor.
	public ConstantPoolStringRef(int index) {
		this.index = index;
	}
	
	@Override
	public ConstantPoolType getType() {
		return ConstantPoolType.STRING_REF;
	}
	
	public static ConstantPoolObject fromInputStream(FancyInputStream inStream) throws IOException {
		return new ConstantPoolStringRef(inStream.readTwoByteInt());
	}
	
	@Override
	public byte[] toBytes() {
		@SuppressWarnings("resource")
		FancyByteArrayOutputStream outStream = new FancyByteArrayOutputStream();
		outStream.write(this.getType().getTagByte());
		outStream.writeTwoByteInteger(this.index);
		return outStream.toByteArray();
	}
	
	public int getIndex() {
		return this.index;
	}
	
	public void setIndex(int index) {
		this.index = index;
	}
	
	@Override
	public String val(ClassConstantPool constPool) {
		return "String reference index: " + this.index + "->"
				+ (constPool != null && constPool.size() >= this.index ?
						constPool.get(this.index).val(constPool) : "~UNKNOWN VALUE");
	}
	
	@Override
	public String simpleVal(ClassConstantPool constPool) {
		return (constPool != null && constPool.size() >= this.index ?
				"\"" + constPool.get(this.index).simpleVal(constPool) + "\"" : "~UNKNOWN_VALUE");
	}
}
