package io.github.pieter12345.wbce.attribute;

import java.io.IOException;

import io.github.pieter12345.wbce.ClassConstantPool;
import io.github.pieter12345.wbce.FancyByteArrayOutputStream;
import io.github.pieter12345.wbce.FancyInputStream;

public class SignatureAttribute implements IAttribute {
	
	// Variables & Constants.
	private int attrNameIndex; // Index of the "Signature" string in the constant pool.
	private int signatureIndex;
	
	public SignatureAttribute(int attrNameIndex, int signatureIndex) {
		this.attrNameIndex = attrNameIndex;
		this.signatureIndex = signatureIndex;
	}
	
	public static SignatureAttribute fromInputStream(int attrNameIndex, FancyInputStream inStream) throws IOException {
		inStream.readFourByteInt(); // attrInfoLength.
		int signatureIndex = inStream.readTwoByteInt();
		return new SignatureAttribute(attrNameIndex, signatureIndex);
	}
	
	@Override
	public byte[] toBytes() {
		@SuppressWarnings("resource")
		FancyByteArrayOutputStream outStream = new FancyByteArrayOutputStream();
		outStream.writeTwoByteInteger(this.signatureIndex);
		byte[] outStreamBytes = outStream.toByteArray();
		
		@SuppressWarnings("resource")
		FancyByteArrayOutputStream outStream2 = new FancyByteArrayOutputStream();
		outStream2.writeTwoByteInteger(this.attrNameIndex);
		outStream2.writeFourByteInteger(outStreamBytes.length);
		outStream2.write(outStreamBytes);
		
		return outStream2.toByteArray();
	}
	
	public int getSignatureIndex() {
		return this.signatureIndex;
	}
	
	public void setSignatureIndex(int signatureIndex) {
		this.signatureIndex = signatureIndex;
	}
	
	@Override
	public String toString(ClassConstantPool constPool) {
		return "SignatureAttribute: {" + (constPool != null && constPool.size() >= this.signatureIndex ?
				constPool.get(this.signatureIndex).val(constPool) : "~UNKNOWN_SIGNATURE") + "}";
	}

}
