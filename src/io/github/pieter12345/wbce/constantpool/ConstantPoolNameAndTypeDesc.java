package io.github.pieter12345.wbce.constantpool;

import java.io.IOException;

import io.github.pieter12345.wbce.ClassConstantPool;
import io.github.pieter12345.wbce.FancyByteArrayOutputStream;
import io.github.pieter12345.wbce.FancyInputStream;
import io.github.pieter12345.wbce.ClassConstantPool.ConstantPoolType;

public class ConstantPoolNameAndTypeDesc implements ConstantPoolObject {
	
	// Variables & Constants.
	private int nameIndex;
	private int typeDescIndex;
	
	// Constructor.
	public ConstantPoolNameAndTypeDesc(int nameIndex, int typeDescIndex) {
		this.nameIndex = nameIndex;
		this.typeDescIndex = typeDescIndex;
	}
	
	@Override
	public ConstantPoolType getType() {
		return ConstantPoolType.NAME_AND_TYPE_DESC;
	}
	
	public static ConstantPoolObject fromInputStream(FancyInputStream inStream) throws IOException {
		return new ConstantPoolNameAndTypeDesc(inStream.readTwoByteInt(), inStream.readTwoByteInt());
	}
	
	@Override
	public byte[] toBytes() {
		@SuppressWarnings("resource")
		FancyByteArrayOutputStream outStream = new FancyByteArrayOutputStream();
		outStream.write(this.getType().getTagByte());
		outStream.writeTwoByteInteger(this.nameIndex);
		outStream.writeTwoByteInteger(this.typeDescIndex);
		return outStream.toByteArray();
	}
	
	public int getNameIndex() {
		return this.nameIndex;
	}
	
	public void setNameIndex(int index) {
		this.nameIndex = index;
	}
	
	public int getTypeDescIndex() {
		return this.typeDescIndex;
	}
	
	public void setTypeDescIndex(int index) {
		this.typeDescIndex = index;
	}
	
	@Override
	public String val(ClassConstantPool constPool) {
		return "Name & Type desc: Name index: " + this.nameIndex + "->"
				+ (constPool != null && constPool.size() >= this.nameIndex ? constPool.get(this.nameIndex).val(constPool) : "~UNKNOWN VALUE")
				+ " Type desc index: " + this.typeDescIndex + "->"
				+ (constPool != null && constPool.size() >= this.typeDescIndex ? constPool.get(this.typeDescIndex).val(constPool) : "~UNKNOWN VALUE");
	}
	
	@Override
	public String simpleVal(ClassConstantPool constPool) {
		return (constPool != null && constPool.size() >= this.nameIndex ?
						constPool.get(this.nameIndex).simpleVal(constPool) : "~UNKNOWN_VALUE")
				+ " " + (constPool != null && constPool.size() >= this.typeDescIndex ?
						constPool.get(this.typeDescIndex).simpleVal(constPool) : "~UNKNOWN_VALUE");
	}
}
