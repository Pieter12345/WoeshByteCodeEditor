package io.github.pieter12345.wbce.attribute;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import io.github.pieter12345.wbce.ClassConstantPool;
import io.github.pieter12345.wbce.FancyByteArrayOutputStream;
import io.github.pieter12345.wbce.FancyInputStream;

public class SourceDebugExtensionAttribute implements IAttribute {
	
	// Variables & Constants.
	private int attrNameIndex; // Index of the "SourceDebugExtension" string in the constant pool.
	private byte[] strBytes;
	
	public SourceDebugExtensionAttribute(int attrNameIndex, byte[] strBytes) {
		this.attrNameIndex = attrNameIndex;
		this.strBytes = strBytes;
	}
	
	public static SourceDebugExtensionAttribute fromInputStream(int attrNameIndex, FancyInputStream inStream) throws IOException {
		int attrInfoLength = inStream.readFourByteInt();
		byte[] strBytes = new byte[attrInfoLength];
		int readCount = inStream.read(strBytes);
		if(readCount != strBytes.length) {
			throw new IOException("Could not read enough bytes.");
		}
		return new SourceDebugExtensionAttribute(attrNameIndex, strBytes);
	}
	
	@Override
	public byte[] toBytes() {
		@SuppressWarnings("resource")
		FancyByteArrayOutputStream outStream = new FancyByteArrayOutputStream();
		outStream.writeTwoByteInteger(this.attrNameIndex);
		outStream.writeFourByteInteger(this.strBytes.length);
		outStream.write(this.strBytes);
		return outStream.toByteArray();
	}
	
	public byte[] getStrBytes() {
		return this.strBytes;
	}
	
	public String getStr() {
		return new String(this.strBytes, StandardCharsets.UTF_8);
	}
	
	public void setStrBytes(byte[] strBytes) {
		this.strBytes = strBytes;
	}
	
	@Override
	public String toString(ClassConstantPool constPool) {
		return SourceDebugExtensionAttribute.class.getSimpleName() + ": {"
				+ new String(this.strBytes, StandardCharsets.UTF_8) + "}";
	}
	
}
