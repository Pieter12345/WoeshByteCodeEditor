package io.github.pieter12345.wbce.attribute;

import java.io.IOException;

import io.github.pieter12345.wbce.ClassConstantPool;
import io.github.pieter12345.wbce.FancyByteArrayOutputStream;
import io.github.pieter12345.wbce.FancyInputStream;

public class SyntheticAttribute implements IAttribute {
	
	// Variables & Constants.
	private int attrNameIndex; // Index of the "Synthetic" string in the constant pool.
	
	public SyntheticAttribute(int attrNameIndex) {
		this.attrNameIndex = attrNameIndex;
	}
	
	public static SyntheticAttribute fromInputStream(int attrNameIndex, FancyInputStream inStream) throws IOException {
		inStream.readFourByteInt(); // attrInfoLength (which is always 0).
		return new SyntheticAttribute(attrNameIndex);
	}
	
	@Override
	public byte[] toBytes() {
		@SuppressWarnings("resource")
		FancyByteArrayOutputStream outStream = new FancyByteArrayOutputStream();
		outStream.writeTwoByteInteger(this.attrNameIndex);
		outStream.writeFourByteInteger(0); // attrInfoLength (which is always 0).
		return outStream.toByteArray();
	}
	
	@Override
	public String toString(ClassConstantPool constPool) {
		return SyntheticAttribute.class.getSimpleName();
	}

}
