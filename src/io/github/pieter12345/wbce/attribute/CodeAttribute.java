package io.github.pieter12345.wbce.attribute;

import java.io.IOException;
import java.util.ArrayList;

import io.github.pieter12345.wbce.FancyInputStream;
import io.github.pieter12345.wbce.ClassConstantPool;
import io.github.pieter12345.wbce.FancyByteArrayOutputStream;

public class CodeAttribute implements IAttribute {
	
	// Variables & Constants.
	private int attrNameIndex; // Index of the "Code" string in the constant pool.
	private int maxStack;
	private int maxLocals;
	private byte[] codeBytes;
	private ExceptionTable exceptionTable;
	private AttributeSet attributes;
	
	public CodeAttribute(int attrNameIndex, int maxStack, int maxLocals, byte[] codeBytes,
			ExceptionTable exceptionTable, AttributeSet attributes) {
		this.attrNameIndex = attrNameIndex;
		this.maxStack = maxStack;
		this.maxLocals = maxLocals;
		this.codeBytes = codeBytes;
		this.exceptionTable = exceptionTable;
		this.attributes = attributes;
	}
	
	public static CodeAttribute fromInputStream(int attrNameIndex, FancyInputStream inStream, ClassConstantPool constPool) throws IOException {
		inStream.readFourByteInt(); // attrInfoLength.
		int maxStack = inStream.readTwoByteInt();
		int maxLocals = inStream.readTwoByteInt();
		
		int codeLength = inStream.readFourByteInt();
		byte[] codeBytes = new byte[codeLength];
		int readCount = inStream.read(codeBytes);
		if(readCount != codeBytes.length) {
			throw new IOException("Could not read enough bytes.");
		}
		
		ExceptionTable exceptionTable = ExceptionTable.fromInputStream(inStream);
		AttributeSet attributes = AttributeSet.fromInputStream(inStream, constPool);
		
		return new CodeAttribute(attrNameIndex, maxStack, maxLocals, codeBytes, exceptionTable, attributes);
	}
	
	@Override
	public byte[] toBytes() {
		@SuppressWarnings("resource")
		FancyByteArrayOutputStream outStream = new FancyByteArrayOutputStream();
		outStream.writeTwoByteInteger(this.maxStack);
		outStream.writeTwoByteInteger(this.maxLocals);
		outStream.writeFourByteInteger(this.codeBytes.length);
		outStream.write(this.codeBytes);
		outStream.write(this.exceptionTable.toBytes());
		outStream.write(this.attributes.toBytes());
		byte[] outStreamBytes = outStream.toByteArray();
		
		@SuppressWarnings("resource")
		FancyByteArrayOutputStream outStream2 = new FancyByteArrayOutputStream();
		outStream2.writeTwoByteInteger(this.attrNameIndex);
		outStream2.writeFourByteInteger(outStreamBytes.length);
		outStream2.write(outStreamBytes);
		
		return outStream2.toByteArray();
	}
	
	public int getMaxStack() {
		return this.maxStack;
	}
	
	public void setMaxStack(int maxStack) {
		this.maxStack = maxStack;
	}
	
	public int getMaxLocals() {
		return this.maxLocals;
	}
	
	public void setMaxLocals(int maxLocals) {
		this.maxLocals = maxLocals;
	}
	
	public byte[] getCodeBytes() {
		return this.codeBytes;
	}
	
	public void setCodeBytes(byte[] bytes) {
		this.codeBytes = bytes;
	}
	
	public ExceptionTable getExceptionTable() {
		return this.exceptionTable;
	}
	
	public void setExceptionTable(ExceptionTable table) {
		this.exceptionTable = table;
	}
	
	public AttributeSet getAttributes() {
		return this.attributes;
	}
	
	public void setAttributes(AttributeSet attrSet) {
		this.attributes = attrSet;
	}
	
	@Override
	public String toString(ClassConstantPool constPool) {
		return "CodeAttribute: {max_stack=" + this.maxStack + ", max_locals=" + this.maxLocals
				+ ", code_bytes[" + this.codeBytes.length + "], " + this.exceptionTable.toString(constPool)
				+ ", " + this.attributes.toString(constPool) + "}";
	}
	
	public static class ExceptionTable {
		
		// Variables & Constants.
		private ArrayList<ExceptionTableEntry> exceptionTableEntries;
		
		public ExceptionTable(ArrayList<ExceptionTableEntry> exceptionTableEntries) {
			this.exceptionTableEntries = exceptionTableEntries;
		}
		
		public static ExceptionTable fromInputStream(FancyInputStream inStream) throws IOException {
			int exceptionTableSize = inStream.readTwoByteInt();
			ArrayList<ExceptionTableEntry> exceptionTableEntries = new ArrayList<ExceptionTableEntry>(exceptionTableSize);
			for(int i = 0; i < exceptionTableSize; i++) {
				exceptionTableEntries.add(ExceptionTableEntry.fromInputStream(inStream));
			}
			return new ExceptionTable(exceptionTableEntries);
		}
		
		public byte[] toBytes() {
			@SuppressWarnings("resource")
			FancyByteArrayOutputStream outStream = new FancyByteArrayOutputStream();
			outStream.writeTwoByteInteger(this.exceptionTableEntries.size());
			for(int i = 0; i < this.exceptionTableEntries.size(); i++) {
				outStream.write(this.exceptionTableEntries.get(i).toBytes());
			}
			return outStream.toByteArray();
		}
		
		public String toString(ClassConstantPool constPool) {
			String exceptionTableStr = "ExceptionTable: {";
			for(int i = 0; i < this.exceptionTableEntries.size(); i++) {
				ExceptionTableEntry exceptionTableEntry = this.exceptionTableEntries.get(i);
				exceptionTableStr += i + ": " + exceptionTableEntry.toString(constPool) + " ";
			}
			return exceptionTableStr.trim() + "}";
		}
		
		public ArrayList<ExceptionTableEntry> getTableEntries() {
			return this.exceptionTableEntries;
		}
		
		public void setTableEntries(ArrayList<ExceptionTableEntry> entries) {
			this.exceptionTableEntries = entries;
		}
		
	}
	
	public static class ExceptionTableEntry {
		
		// Variables & Constants.
		private int startPc;
		private int endPc;
		private int handlerPc;
		private int catchTypeIndex;
		
		public ExceptionTableEntry(int startPc, int endPc, int handlerPc, int catchTypeIndex) {
			this.startPc = startPc;
			this.endPc = endPc;
			this.handlerPc = handlerPc;
			this.catchTypeIndex = catchTypeIndex;
		}
		
		public static ExceptionTableEntry fromInputStream(FancyInputStream inStream) throws IOException {
			int startPc = inStream.readTwoByteInt();
			int endPc = inStream.readTwoByteInt();
			int handlerPc = inStream.readTwoByteInt();
			int catchTypeIndex = inStream.readTwoByteInt();
			return new ExceptionTableEntry(startPc, endPc, handlerPc, catchTypeIndex);
		}
		
		public byte[] toBytes() {
			@SuppressWarnings("resource")
			FancyByteArrayOutputStream outStream = new FancyByteArrayOutputStream();
			outStream.writeTwoByteInteger(this.startPc);
			outStream.writeTwoByteInteger(this.endPc);
			outStream.writeTwoByteInteger(this.handlerPc);
			outStream.writeTwoByteInteger(this.catchTypeIndex);
			return outStream.toByteArray();
		}
		
		public int getStartPc() {
			return this.startPc;
		}
		
		public void setStartPc(int startPc) {
			this.startPc = startPc;
		}
		
		public int getEndPc() {
			return this.endPc;
		}
		
		public void setEndPc(int endPc) {
			this.endPc = endPc;
		}
		
		public int getHandlerPc() {
			return this.handlerPc;
		}
		
		public void setHandlerPc(int handlerPc) {
			this.handlerPc = handlerPc;
		}
		
		public int getCatchTypeIndex() {
			return this.catchTypeIndex;
		}
		
		public void setCatchTypeIndex(int catchTypeIndex) {
			this.catchTypeIndex = catchTypeIndex;
		}
		
		public String toString(ClassConstantPool constPool) {
			return "ExceptionTableEntry: {start_pc=" + this.startPc + ", end_pc=" + this.endPc
					+ ", handlerPc=" + this.handlerPc + ", catchTypeIndex="
					+ (this.catchTypeIndex == 0 ? "~ALL_EXCEPTIONS" : (constPool != null && constPool.size() >= this.catchTypeIndex ?
					constPool.get(this.catchTypeIndex).val(constPool) : "~UNKNOWN_CATCH_TYPE")) + "}";
		}
	}
	
}
