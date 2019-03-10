package io.github.pieter12345.wbce.attribute;

import java.io.IOException;

import io.github.pieter12345.wbce.ClassConstantPool;
import io.github.pieter12345.wbce.FancyByteArrayOutputStream;
import io.github.pieter12345.wbce.FancyInputStream;

public class SourceFileAttribute implements IAttribute {
	
	// Variables & Constants.
	private int attrNameIndex; // Index of the "SourceFile" string in the constant pool.
	private int sourceFileNameIndex;
	
	public SourceFileAttribute(int attrNameIndex, int valueIndex) {
		this.attrNameIndex = attrNameIndex;
		this.sourceFileNameIndex = valueIndex;
	}
	
	public static SourceFileAttribute fromInputStream(int attrNameIndex, FancyInputStream inStream) throws IOException {
		inStream.readFourByteInt(); // attrInfoLength.
		int valueIndex = inStream.readTwoByteInt();
		return new SourceFileAttribute(attrNameIndex, valueIndex);
	}
	
	@Override
	public byte[] toBytes() {
		@SuppressWarnings("resource")
		FancyByteArrayOutputStream outStream = new FancyByteArrayOutputStream();
		outStream.writeTwoByteInteger(this.sourceFileNameIndex);
		byte[] outStreamBytes = outStream.toByteArray();
		
		@SuppressWarnings("resource")
		FancyByteArrayOutputStream outStream2 = new FancyByteArrayOutputStream();
		outStream2.writeTwoByteInteger(this.attrNameIndex);
		outStream2.writeFourByteInteger(outStreamBytes.length);
		outStream2.write(outStreamBytes);
		
		return outStream2.toByteArray();
	}
	
	public int getSourceFileNameIndex() {
		return this.sourceFileNameIndex;
	}
	
	public void setSourceFileNameIndex(int valueIndex) {
		this.sourceFileNameIndex = valueIndex;
	}
	
	@Override
	public String toString(ClassConstantPool constPool) {
		return "SourceFileAttribute: {" + (constPool != null && constPool.size() >= this.sourceFileNameIndex ?
				constPool.get(this.sourceFileNameIndex).val(constPool) : "~UNKNOWN_SOURCEFILE_NAME") + "}";
	}

}
