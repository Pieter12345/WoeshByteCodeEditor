package io.github.pieter12345.wbce.attribute;

import java.io.IOException;

import io.github.pieter12345.wbce.ClassConstantPool;
import io.github.pieter12345.wbce.FancyByteArrayOutputStream;
import io.github.pieter12345.wbce.FancyInputStream;

public class DeprecatedAttribute implements IAttribute {
	
	// Variables & Constants.
	private int attrNameIndex; // Index of the "Deprecated" string in the constant pool.
	
	public DeprecatedAttribute(int attrNameIndex) {
		this.attrNameIndex = attrNameIndex;
	}
	
	public static DeprecatedAttribute fromInputStream(int attrNameIndex, FancyInputStream inStream) throws IOException {
		inStream.readFourByteInt(); // attrInfoLength (which is always 0).
		return new DeprecatedAttribute(attrNameIndex);
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
		return DeprecatedAttribute.class.getSimpleName();
	}

}
