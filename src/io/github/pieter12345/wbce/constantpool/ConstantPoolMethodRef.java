package io.github.pieter12345.wbce.constantpool;

import java.io.IOException;

import io.github.pieter12345.wbce.ClassConstantPool;
import io.github.pieter12345.wbce.FancyByteArrayOutputStream;
import io.github.pieter12345.wbce.FancyInputStream;
import io.github.pieter12345.wbce.ClassConstantPool.ConstantPoolType;

public class ConstantPoolMethodRef implements ConstantPoolObject {
	
	// Variables & Constants.
	private int classIndex;
	private int nameAndTypeIndex;
	
	// Constructor.
	public ConstantPoolMethodRef(int classIndex, int nameAndTypeIndex) {
		this.classIndex = classIndex;
		this.nameAndTypeIndex = nameAndTypeIndex;
	}
	
	@Override
	public ConstantPoolType getType() {
		return ConstantPoolType.METHOD_REF;
	}
	
	public static ConstantPoolObject fromInputStream(FancyInputStream inStream) throws IOException {
		return new ConstantPoolMethodRef(inStream.readTwoByteInt(), inStream.readTwoByteInt());
	}
	
	@Override
	public byte[] toBytes() {
		@SuppressWarnings("resource")
		FancyByteArrayOutputStream outStream = new FancyByteArrayOutputStream();
		outStream.write(this.getType().getTagByte());
		outStream.writeTwoByteInteger(this.classIndex);
		outStream.writeTwoByteInteger(this.nameAndTypeIndex);
		return outStream.toByteArray();
	}
	
	public int getClassIndex() {
		return this.classIndex;
	}
	
	public void setClassIndex(int index) {
		this.classIndex = index;
	}
	
	public int getNameAndTypeIndex() {
		return this.nameAndTypeIndex;
	}
	
	public void setNameAndTypeIndex(int index) {
		this.nameAndTypeIndex = index;
	}
	
	@Override
	public String val(ClassConstantPool constPool) {
		return "Method reference: Class reference index: " + this.classIndex + "->"
				+ (constPool != null && constPool.size() >= this.classIndex ? constPool.get(this.classIndex).val(constPool) : "~UNKNOWN VALUE")
				+ " Name & Type desc index: " + this.nameAndTypeIndex + "->"
				+ (constPool != null && constPool.size() >= this.nameAndTypeIndex ? constPool.get(this.nameAndTypeIndex).val(constPool) : "~UNKNOWN VALUE");
	}
	
	@Override
	public String simpleVal(ClassConstantPool constPool) {
		return (constPool != null && constPool.size() >= this.classIndex ?
						constPool.get(this.classIndex).simpleVal(constPool) : "~UNKNOWN_VALUE")
				+ " " + (constPool != null && constPool.size() >= this.nameAndTypeIndex ?
						constPool.get(this.nameAndTypeIndex).simpleVal(constPool) : "~UNKNOWN_VALUE");
	}
}
