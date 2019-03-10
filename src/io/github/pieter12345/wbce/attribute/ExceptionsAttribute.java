package io.github.pieter12345.wbce.attribute;

import java.io.IOException;
import java.util.ArrayList;

import io.github.pieter12345.wbce.ClassConstantPool;
import io.github.pieter12345.wbce.FancyByteArrayOutputStream;
import io.github.pieter12345.wbce.FancyInputStream;

public class ExceptionsAttribute implements IAttribute {
	
	// Variables & Constants.
	private int attrNameIndex; // Index of the "Exceptions" string in the constant pool.
	private ArrayList<ExceptionIndex> exceptionIndices;
	
	public ExceptionsAttribute(int attrNameIndex, ArrayList<ExceptionIndex> exceptionIndices) {
		this.attrNameIndex = attrNameIndex;
		this.exceptionIndices = exceptionIndices;
	}
	
	public static ExceptionsAttribute fromInputStream(int attrNameIndex, FancyInputStream inStream) throws IOException {
		inStream.readFourByteInt(); // attrInfoLength.
		int exceptionIndexCount = inStream.readTwoByteInt();
		ArrayList<ExceptionIndex> exceptionIndices = new ArrayList<ExceptionIndex>(exceptionIndexCount);
		for(int i = 0; i < exceptionIndexCount; i++) {
			exceptionIndices.add(ExceptionIndex.fromInputStream(inStream));
		}
		return new ExceptionsAttribute(attrNameIndex, exceptionIndices);
	}
	
	@Override
	public byte[] toBytes() {
		@SuppressWarnings("resource")
		FancyByteArrayOutputStream outStream = new FancyByteArrayOutputStream();
		outStream.writeTwoByteInteger(this.exceptionIndices.size());
		for(int i = 0; i < this.exceptionIndices.size(); i++) {
			outStream.write(this.exceptionIndices.get(i).toBytes());
		}
		byte[] outStreamBytes = outStream.toByteArray();
		
		@SuppressWarnings("resource")
		FancyByteArrayOutputStream outStream2 = new FancyByteArrayOutputStream();
		outStream2.writeTwoByteInteger(this.attrNameIndex);
		outStream2.writeFourByteInteger(outStreamBytes.length);
		outStream2.write(outStreamBytes);
		
		return outStream2.toByteArray();
	}
	
	public ExceptionIndex getExceptionIndex(int index) {
		return this.exceptionIndices.get(index);
	}
	
	public void setExceptionIndex(int index, ExceptionIndex exceptionIndex) {
		this.exceptionIndices.set(index, exceptionIndex);
	}
	
	public void addExceptionIndex(ExceptionIndex exceptionIndex) {
		this.exceptionIndices.add(exceptionIndex);
	}
	
	public ExceptionIndex removeExceptionIndex(int index) {
		return this.exceptionIndices.remove(index);
	}
	
	public boolean removeExceptionIndex(ExceptionIndex exceptionIndex) {
		return this.exceptionIndices.remove(exceptionIndex);
	}
	
	@Override
	public String toString(ClassConstantPool constPool) {
		String exceptionIndexStr = "";
		for(int i = 0; i < this.exceptionIndices.size(); i++) {
			exceptionIndexStr += i + ": " + this.exceptionIndices.get(i).toString(constPool) + " ";
		}
		return "ExceptionsAttribute: {" + exceptionIndexStr.trim() + "}";
	}
	
	public static class ExceptionIndex {
		
		// Variables & Constants.
		private int exceptionNameIndex;
		
		public ExceptionIndex(int exceptionNameIndex) {
			this.exceptionNameIndex = exceptionNameIndex;
		}
		
		public static ExceptionIndex fromInputStream(FancyInputStream inStream) throws IOException {
			int exceptionNameIndex = inStream.readTwoByteInt();
			return new ExceptionIndex(exceptionNameIndex);
		}
		
		public byte[] toBytes() {
			@SuppressWarnings("resource")
			FancyByteArrayOutputStream outStream = new FancyByteArrayOutputStream();
			outStream.writeTwoByteInteger(this.exceptionNameIndex);
			return outStream.toByteArray();
		}
		
		public int getExceptionNameIndex() {
			return this.exceptionNameIndex;
		}
		
		public void setExceptionNameIndex(int exceptionNameIndex) {
			this.exceptionNameIndex = exceptionNameIndex;
		}
		
		public String toString(ClassConstantPool constPool) {
			return this.exceptionNameIndex + "->" + (constPool != null && constPool.size() >= this.exceptionNameIndex ?
					constPool.get(this.exceptionNameIndex).val(constPool) : "~UNKNOWN_EXCEPTION_NAME");
//			return "ExceptionIndex: {ExceptionNameIndex=" + this.exceptionNameIndex + "->"
//					+ (constPool != null && constPool.size() >= this.exceptionNameIndex ?
//					constPool.get(this.exceptionNameIndex).val(constPool) : "~UNKNOWN_EXCEPTION_NAME") + "}";
		}
		
	}

}
