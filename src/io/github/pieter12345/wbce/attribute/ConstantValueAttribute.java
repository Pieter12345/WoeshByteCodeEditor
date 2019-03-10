package io.github.pieter12345.wbce.attribute;

import java.io.IOException;

import io.github.pieter12345.wbce.ClassConstantPool;
import io.github.pieter12345.wbce.FancyByteArrayOutputStream;
import io.github.pieter12345.wbce.FancyInputStream;

public class ConstantValueAttribute implements IAttribute {
	
	// Variables & Constants.
	private int attrNameIndex; // Index of the "ConstantValue" string in the constant pool.
	private int valueIndex;
	
	public ConstantValueAttribute(int attrNameIndex, int valueIndex) {
		this.attrNameIndex = attrNameIndex;
		this.valueIndex = valueIndex;
	}
	
	public static ConstantValueAttribute fromInputStream(int attrNameIndex, FancyInputStream inStream) throws IOException {
		inStream.readFourByteInt(); // attrInfoLength.
		int valueIndex = inStream.readTwoByteInt();
		return new ConstantValueAttribute(attrNameIndex, valueIndex);
	}
	
	@Override
	public byte[] toBytes() {
		@SuppressWarnings("resource")
		FancyByteArrayOutputStream outStream = new FancyByteArrayOutputStream();
		outStream.writeTwoByteInteger(this.valueIndex);
		byte[] outStreamBytes = outStream.toByteArray();
		
		@SuppressWarnings("resource")
		FancyByteArrayOutputStream outStream2 = new FancyByteArrayOutputStream();
		outStream2.writeTwoByteInteger(this.attrNameIndex);
		outStream2.writeFourByteInteger(outStreamBytes.length);
		outStream2.write(outStreamBytes);
		
		return outStream2.toByteArray();
	}
	
	public int getValueIndex() {
		return this.valueIndex;
	}
	
	public void setValueIndex(int valueIndex) {
		this.valueIndex = valueIndex;
	}
	
	@Override
	public String toString(ClassConstantPool constPool) {
		return "ConstantValueAttribute: {" + (constPool != null && constPool.size() >= this.valueIndex ?
				constPool.get(this.valueIndex).val(constPool) : "~UNKNOWN_VALUE") + "}";
	}

}
