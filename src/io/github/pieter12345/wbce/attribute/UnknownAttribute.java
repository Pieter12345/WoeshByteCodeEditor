package io.github.pieter12345.wbce.attribute;

import java.io.IOException;

import io.github.pieter12345.wbce.ClassConstantPool;
import io.github.pieter12345.wbce.FancyByteArrayOutputStream;
import io.github.pieter12345.wbce.FancyInputStream;

public class UnknownAttribute implements IAttribute {
	
	// Variables & Constants.
	public int attrNameIndex;
	public byte[] attrInfoBytes;
	
	public UnknownAttribute(int attrNameIndex, byte[] attrInfoBytes) {
		this.attrNameIndex = attrNameIndex;
		this.attrInfoBytes = attrInfoBytes;
	}
	
	public static UnknownAttribute fromInputStream(int attrNameIndex, FancyInputStream inStream) throws IOException {
		int attrInfoLength = inStream.readFourByteInt();
		byte[] attrInfoBytes = new byte[attrInfoLength];
		int readCount = inStream.read(attrInfoBytes);
		if(readCount != attrInfoBytes.length) {
			throw new IOException("Could not read enough bytes.");
		}
		return new UnknownAttribute(attrNameIndex, attrInfoBytes);
	}
	
	public int getNameIndex() {
		return this.attrNameIndex;
	}
	
	public void setNameIndex(int nameIndex) {
		this.attrNameIndex = nameIndex;
	}
	
	public byte[] getBytes() {
		return this.attrInfoBytes;
	}
	
	public void setBytes(byte[] attrInfoBytes) {
		this.attrInfoBytes = attrInfoBytes;
	}
	
	@Override
	public byte[] toBytes() {
		@SuppressWarnings("resource")
		FancyByteArrayOutputStream outStream = new FancyByteArrayOutputStream();
		outStream.writeTwoByteInteger(this.attrNameIndex);
		outStream.writeFourByteInteger(this.attrInfoBytes.length);
		outStream.write(this.attrInfoBytes);
		return outStream.toByteArray();
	}
	
	@Override
	public String toString(ClassConstantPool constPool) {
		return "UnknownAttribute: {" + (constPool != null && constPool.size() >= this.attrNameIndex ?
				constPool.get(this.attrNameIndex).val(constPool) : "~UNKNOWN_ATTR_NAME")
				+ " (size " + this.attrInfoBytes.length + ")}";
	}
	
}