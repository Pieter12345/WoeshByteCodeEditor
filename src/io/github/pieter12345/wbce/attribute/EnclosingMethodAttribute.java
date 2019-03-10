package io.github.pieter12345.wbce.attribute;

import java.io.IOException;

import io.github.pieter12345.wbce.ClassConstantPool;
import io.github.pieter12345.wbce.FancyByteArrayOutputStream;
import io.github.pieter12345.wbce.FancyInputStream;

public class EnclosingMethodAttribute implements IAttribute {
	
	// Variables & Constants.
	private int attrNameIndex; // Index of the "EnclosingMethod" string in the constant pool.
	private int classIndex;
	private int methodIndex;
	
	public EnclosingMethodAttribute(int attrNameIndex, int classIndex, int methodIndex) {
		this.attrNameIndex = attrNameIndex;
		this.classIndex = classIndex;
		this.methodIndex = methodIndex;
	}
	
	public static EnclosingMethodAttribute fromInputStream(int attrNameIndex, FancyInputStream inStream) throws IOException {
		inStream.readFourByteInt(); // attrInfoLength.
		int classIndex = inStream.readTwoByteInt();
		int methodIndex = inStream.readTwoByteInt();
		return new EnclosingMethodAttribute(attrNameIndex, classIndex, methodIndex);
	}
	
	@Override
	public byte[] toBytes() {
		@SuppressWarnings("resource")
		FancyByteArrayOutputStream outStream = new FancyByteArrayOutputStream();
		outStream.writeTwoByteInteger(this.classIndex);
		outStream.writeTwoByteInteger(this.methodIndex);
		byte[] outStreamBytes = outStream.toByteArray();
		
		@SuppressWarnings("resource")
		FancyByteArrayOutputStream outStream2 = new FancyByteArrayOutputStream();
		outStream2.writeTwoByteInteger(this.attrNameIndex);
		outStream2.writeFourByteInteger(outStreamBytes.length);
		outStream2.write(outStreamBytes);
		
		return outStream2.toByteArray();
	}
	
	public int getClassIndex() {
		return this.classIndex;
	}
	
	public void setClassIndex(int classIndex) {
		this.classIndex = classIndex;
	}
	
	public int getMethodIndex() {
		return this.methodIndex;
	}
	
	public void setMethodIndex(int methodIndex) {
		this.methodIndex = methodIndex;
	}
	
	@Override
	public String toString(ClassConstantPool constPool) {
		String _class = (constPool != null && constPool.size() >= this.classIndex ?
				constPool.get(this.classIndex).val(constPool) : "~UNKNOWN_CLASS");
		String method = (constPool != null && constPool.size() >= this.methodIndex ?
				constPool.get(this.methodIndex).val(constPool) : "~UNKNOWN_METHOD");
		return EnclosingMethodAttribute.class.getSimpleName() + ": {"
				+ "class_index=" + this.classIndex + "->" + _class
				+ ", method_index=" + this.methodIndex + "->" + method + "}";
	}

}
