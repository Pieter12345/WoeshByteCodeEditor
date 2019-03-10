package io.github.pieter12345.wbce.constantpool;

import java.io.IOException;

import io.github.pieter12345.wbce.ClassConstantPool;
import io.github.pieter12345.wbce.FancyByteArrayOutputStream;
import io.github.pieter12345.wbce.FancyInputStream;
import io.github.pieter12345.wbce.ClassConstantPool.ConstantPoolType;

public class ConstantPoolInvokeDynamic implements ConstantPoolObject {
	
	// Variables & Constants.
	private int bootstrapMethodAttrIndex;
	private int nameAndTypeDescIndex;
	
	// Constructor.
	public ConstantPoolInvokeDynamic(int nameIndex, int typeDescIndex) {
		this.bootstrapMethodAttrIndex = nameIndex;
		this.nameAndTypeDescIndex = typeDescIndex;
	}
	
	@Override
	public ConstantPoolType getType() {
		return ConstantPoolType.INVOKEDYNAMIC;
	}
	
	public static ConstantPoolObject fromInputStream(FancyInputStream inStream) throws IOException {
		return new ConstantPoolInvokeDynamic(inStream.readTwoByteInt(), inStream.readTwoByteInt());
	}
	
	@Override
	public byte[] toBytes() {
		@SuppressWarnings("resource")
		FancyByteArrayOutputStream outStream = new FancyByteArrayOutputStream();
		outStream.write(this.getType().getTagByte());
		outStream.writeTwoByteInteger(this.bootstrapMethodAttrIndex);
		outStream.writeTwoByteInteger(this.nameAndTypeDescIndex);
		return outStream.toByteArray();
	}
	
	public int getBootstrapMethodAttrIndex() {
		return this.bootstrapMethodAttrIndex;
	}
	
	public void setBootstrapMethodAttrIndex(int index) {
		this.bootstrapMethodAttrIndex = index;
	}
	
	public int getNameAndTypeDescIndex() {
		return this.nameAndTypeDescIndex;
	}
	
	public void setNameAndTypeDescIndex(int index) {
		this.nameAndTypeDescIndex = index;
	}
	
	@Override
	public String val(ClassConstantPool constPool) {
		return "InvokeDynamic: Bootstrap method attr index: " + this.bootstrapMethodAttrIndex + "->"
				+ (constPool != null && constPool.size() >= this.bootstrapMethodAttrIndex ? constPool.get(this.bootstrapMethodAttrIndex).val(constPool) : "~UNKNOWN VALUE")
				+ " Name & Type desc index: " + this.nameAndTypeDescIndex + "->"
				+ (constPool != null && constPool.size() >= this.nameAndTypeDescIndex ? constPool.get(this.nameAndTypeDescIndex).val(constPool) : "~UNKNOWN VALUE");
	}
	
	@Override
	public String simpleVal(ClassConstantPool constPool) {
		return (constPool != null && constPool.size() >= this.bootstrapMethodAttrIndex ?
						constPool.get(this.bootstrapMethodAttrIndex).simpleVal(constPool) : "~UNKNOWN_VALUE")
				+ " " + (constPool != null && constPool.size() >= this.nameAndTypeDescIndex ?
						constPool.get(this.nameAndTypeDescIndex).simpleVal(constPool) : "~UNKNOWN_VALUE");
	}
}
