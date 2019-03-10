package io.github.pieter12345.wbce;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FancyInputStream extends BufferedInputStream {
	
	public FancyInputStream(InputStream in, int size) {
		super(in, size);
	}
	
	public FancyInputStream(InputStream in) {
		super(in);
	}
	
	public int readTwoByteInt() throws IOException {
		byte[] buff = new byte[2];
		if(this.read(buff, 0, 2) == -1) {
			throw new IOException("End of stream.");
		}
		return ((buff[0] & 0xFF) << 8) + (buff[1] & 0xFF);
	}
	
	public int readFourByteInt() throws IOException {
		byte[] buff = new byte[4];
		if(this.read(buff, 0, 4) == -1) {
			throw new IOException("End of stream.");
		}
		return ((buff[0] & 0xFF) << 24) + ((buff[1] & 0xFF) << 16) + ((buff[2] & 0xFF) << 8) + (buff[3] & 0xFF);
	}
	
	public byte readByte() throws IOException {
		byte[] buff = new byte[1];
		if(this.read(buff, 0, 1) == -1) {
			throw new IOException("End of stream.");
		}
		return buff[0];
	}
	
	public int readUnsignedByte() throws IOException {
		return this.readByte() & 0xFF;
	}
	
	public long readLong() throws IOException {
		byte[] buff = new byte[8];
		if(this.read(buff, 0, 8) == -1) {
			throw new IOException("End of stream.");
		}
		long ret = 0L;
		for(int i = 0; i < 8; i++) {
			ret += ((buff[i] & 0xFF) << (7 - i) * 8);
		}
		return ret;
	}
	
	public float readFloat() throws IOException {
		int floatBits = this.readFourByteInt();
		return Float.intBitsToFloat(floatBits);
	}
	
	public double readDouble() throws IOException {
		long doubleBits = this.readLong();
		return Double.longBitsToDouble(doubleBits);
	}
}
