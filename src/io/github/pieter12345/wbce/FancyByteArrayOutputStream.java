package io.github.pieter12345.wbce;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class FancyByteArrayOutputStream extends ByteArrayOutputStream {
	
	// Override method without "throws" since it can't throw an IOException.
	@Override
	public void write(byte b[]) {
        super.write(b, 0, b.length);
    }
	
	public void writeString(String value) {
		this.write(value.getBytes(StandardCharsets.UTF_8));
	}
	
	public void writeString(String value, Charset charset) {
		this.write(value.getBytes(charset));
	}
	
	public void writeByte(byte value) {
		this.write(value);
	}
	
	public void writeUnsignedByte(int value) {
		this.write(value);
	}
	
	public void writeTwoByteInteger(int value) {
		byte[] bytes = new byte[2];
		bytes[0] = (byte) ((value >>> 8) & 0xFF);
		bytes[1] = (byte) (value & 0xFF);
		this.write(bytes);
	}
	
	public void writeFourByteInteger(int value) {
		byte[] bytes = new byte[4];
		bytes[0] = (byte) ((value >>> 24) & 0xFF);
		bytes[1] = (byte) ((value >>> 16) & 0xFF);
		bytes[2] = (byte) ((value >>> 8) & 0xFF);
		bytes[3] = (byte) (value & 0xFF);
		this.write(bytes);
	}
	
	public void writeFloat(float value) {
		int floatBits = Float.floatToRawIntBits(value);
		this.writeFourByteInteger(floatBits);
	}
	
	public void writeLong(long value) {
		byte[] bytes = new byte[8];
		for(int i = 0; i < bytes.length; i++) {
			bytes[i] = (byte) ((value >>> (bytes.length - 1 - i) * 8) & 0xFF);
		}
		this.write(bytes);
	}
	
	public void writeDouble(double value) {
		long doubleBits = Double.doubleToLongBits(value);
		this.writeLong(doubleBits);
	}
	
	
	
}
