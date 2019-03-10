package io.github.pieter12345.wbce.attribute;

import java.io.IOException;
import java.util.ArrayList;

import io.github.pieter12345.wbce.ClassConstantPool;
import io.github.pieter12345.wbce.FancyByteArrayOutputStream;
import io.github.pieter12345.wbce.FancyInputStream;

public class LineNumberTableAttribute implements IAttribute {
	
	// Variables & Constants.
	private int attrNameIndex; // Index of the "LineNumberTable" string in the constant pool.
	private ArrayList<LineNumberEntry> lineNumerEntries;
	
	public LineNumberTableAttribute(int attrNameIndex, ArrayList<LineNumberEntry> lineNumerEntries) {
		this.attrNameIndex = attrNameIndex;
		this.lineNumerEntries = lineNumerEntries;
	}
	
	public static LineNumberTableAttribute fromInputStream(int attrNameIndex, FancyInputStream inStream) throws IOException {
		inStream.readFourByteInt(); // attrInfoLength.
		int lineNumberEntryCount = inStream.readTwoByteInt();
		ArrayList<LineNumberEntry> lineNumberTable = new ArrayList<LineNumberEntry>(lineNumberEntryCount);
		for(int i = 0; i < lineNumberEntryCount; i++) {
			lineNumberTable.add(LineNumberEntry.fromInputStream(inStream));
		}
		return new LineNumberTableAttribute(attrNameIndex, lineNumberTable);
	}
	
	@Override
	public byte[] toBytes() {
		@SuppressWarnings("resource")
		FancyByteArrayOutputStream outStream = new FancyByteArrayOutputStream();
		outStream.writeTwoByteInteger(this.lineNumerEntries.size());
		for(int i = 0; i < this.lineNumerEntries.size(); i++) {
			outStream.write(this.lineNumerEntries.get(i).toBytes());
		}
		byte[] outStreamBytes = outStream.toByteArray();
		
		@SuppressWarnings("resource")
		FancyByteArrayOutputStream outStream2 = new FancyByteArrayOutputStream();
		outStream2.writeTwoByteInteger(this.attrNameIndex);
		outStream2.writeFourByteInteger(outStreamBytes.length);
		outStream2.write(outStreamBytes);
		
		return outStream2.toByteArray();
	}
	
	public LineNumberEntry getLineNumberEntry(int index) {
		return this.lineNumerEntries.get(index);
	}
	
	public void setLineNumberEntry(int index, LineNumberEntry lineNumberEntry) {
		this.lineNumerEntries.set(index, lineNumberEntry);
	}
	
	public void addLineNumberEntry(LineNumberEntry lineNumberEntry) {
		this.lineNumerEntries.add(lineNumberEntry);
	}
	
	public LineNumberEntry removeLineNumberEntry(int index) {
		return this.lineNumerEntries.remove(index);
	}
	
	public boolean removeLineNumberEntry(LineNumberEntry lineNumberEntry) {
		return this.lineNumerEntries.remove(lineNumberEntry);
	}
	
	@Override
	public String toString(ClassConstantPool constPool) {
		String lineNumberEntryTableStr = "";
		for(int i = 0; i < this.lineNumerEntries.size(); i++) {
			lineNumberEntryTableStr += i + ": " + this.lineNumerEntries.get(i).toString(constPool) + " ";
		}
		return "LineNumberTableAttribute: {" + lineNumberEntryTableStr.trim() + "}";
	}
	
	public static class LineNumberEntry {
		
		// Variables & Constants.
		private int start_pc;
		private int lineNumber;
		public LineNumberEntry(int start_pc, int lineNumber) {
			this.start_pc = start_pc;
			this.lineNumber = lineNumber;
		}
		
		public static LineNumberEntry fromInputStream(FancyInputStream inStream) throws IOException {
			int start_pc = inStream.readTwoByteInt();
			int lineNumber = inStream.readTwoByteInt();
			return new LineNumberEntry(start_pc, lineNumber);
		}
		
		public byte[] toBytes() {
			@SuppressWarnings("resource")
			FancyByteArrayOutputStream outStream = new FancyByteArrayOutputStream();
			outStream.writeTwoByteInteger(this.start_pc);
			outStream.writeTwoByteInteger(this.lineNumber);
			return outStream.toByteArray();
		}
		
		public int getStartPc() {
			return this.start_pc;
		}
		
		public void setStartPc(int start_pc) {
			this.start_pc = start_pc;
		}
		
		public int getLineNumber() {
			return this.lineNumber;
		}
		
		public void setLineNumber(int lineNumber) {
			this.lineNumber = lineNumber;
		}
		
		public String toString(ClassConstantPool constPool) {
			return "LineNumberEntry: {start_pc=" + this.start_pc + ", line_number=" + this.lineNumber + "}";
		}
	}
}
