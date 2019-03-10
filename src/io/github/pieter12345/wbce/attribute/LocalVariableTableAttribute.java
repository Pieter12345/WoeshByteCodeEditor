package io.github.pieter12345.wbce.attribute;

import java.io.IOException;
import java.util.ArrayList;

import io.github.pieter12345.wbce.ClassConstantPool;
import io.github.pieter12345.wbce.FancyByteArrayOutputStream;
import io.github.pieter12345.wbce.FancyInputStream;
import io.github.pieter12345.wbce.constantpool.ConstantPoolString;

public class LocalVariableTableAttribute implements IAttribute {
	
	// Variables & Constants.
	private int attrNameIndex; // Index of the "LocalVariableTable" string in the constant pool.
	private ArrayList<LocalVariable> localVariableTable;
	
	public LocalVariableTableAttribute(int attrNameIndex, ArrayList<LocalVariable> localVariableTable) {
		this.attrNameIndex = attrNameIndex;
		this.localVariableTable = localVariableTable;
	}
	
	public static LocalVariableTableAttribute fromInputStream(int attrNameIndex, FancyInputStream inStream) throws IOException {
		inStream.readFourByteInt(); // attrInfoLength.
		int localVariableCount = inStream.readTwoByteInt();
		ArrayList<LocalVariable> localVariableTable = new ArrayList<LocalVariable>(localVariableCount);
		for(int i = 0; i < localVariableCount; i++) {
			localVariableTable.add(LocalVariable.fromInputStream(inStream));
		}
		return new LocalVariableTableAttribute(attrNameIndex, localVariableTable);
	}
	
	@Override
	public byte[] toBytes() {
		@SuppressWarnings("resource")
		FancyByteArrayOutputStream outStream = new FancyByteArrayOutputStream();
		outStream.writeTwoByteInteger(this.localVariableTable.size());
		for(int i = 0; i < this.localVariableTable.size(); i++) {
			outStream.write(this.localVariableTable.get(i).toBytes());
		}
		byte[] outStreamBytes = outStream.toByteArray();
		
		@SuppressWarnings("resource")
		FancyByteArrayOutputStream outStream2 = new FancyByteArrayOutputStream();
		outStream2.writeTwoByteInteger(this.attrNameIndex);
		outStream2.writeFourByteInteger(outStreamBytes.length);
		outStream2.write(outStreamBytes);
		
		return outStream2.toByteArray();
	}
	
	public LocalVariable getLocalVariable(int index) {
		return this.localVariableTable.get(index);
	}
	
	public void setLocalVariable(int index, LocalVariable localVariable) {
		this.localVariableTable.set(index, localVariable);
	}
	
	public void addLocalVariable(LocalVariable localVariable) {
		this.localVariableTable.add(localVariable);
	}
	
	public LocalVariable removeLocalVariable(int index) {
		return this.localVariableTable.remove(index);
	}
	
	public boolean removeLocalVariable(LocalVariable localVariable) {
		return this.localVariableTable.remove(localVariable);
	}
	
	@Override
	public String toString(ClassConstantPool constPool) {
		String localVariableTableStr = "";
		for(int i = 0; i < this.localVariableTable.size(); i++) {
			localVariableTableStr += i + ": " + this.localVariableTable.get(i).toString(constPool) + " ";
		}
		return LocalVariableTableAttribute.class.getSimpleName() + ": {" + localVariableTableStr.trim() + "}";
	}
	
	public static class LocalVariable {
		
		// Variables & Constants.
		private int start_pc; // The instruction/line at which the variable is defined.
		private int pc_length; // The amount of instructions/lines for which the variable is defined.
		private int nameIndex;
		private int descIndex;
		private int variableIndex; // Index of the variable in the local variable array. Double/Long's also use index+1.
		
		public LocalVariable(int start_pc, int pc_length, int nameIndex, int descIndex, int variableIndex) {
			this.start_pc = start_pc;
			this.pc_length = pc_length;
			this.nameIndex = nameIndex;
			this.descIndex = descIndex;
			this.variableIndex = variableIndex;
		}
		
		public static LocalVariable fromInputStream(FancyInputStream inStream) throws IOException {
			int start_pc = inStream.readTwoByteInt();
			int pc_length = inStream.readTwoByteInt();
			int nameIndex = inStream.readTwoByteInt();
			int descIndex = inStream.readTwoByteInt();
			int variableIndex = inStream.readTwoByteInt();
			return new LocalVariable(start_pc, pc_length, nameIndex, descIndex, variableIndex);
		}
		
		public byte[] toBytes() {
			@SuppressWarnings("resource")
			FancyByteArrayOutputStream outStream = new FancyByteArrayOutputStream();
			outStream.writeTwoByteInteger(this.start_pc);
			outStream.writeTwoByteInteger(this.pc_length);
			outStream.writeTwoByteInteger(this.nameIndex);
			outStream.writeTwoByteInteger(this.descIndex);
			outStream.writeTwoByteInteger(this.variableIndex);
			return outStream.toByteArray();
		}
		
		public int getStartPc() {
			return this.start_pc;
		}
		
		public void setStartPc(int start_pc) {
			this.start_pc = start_pc;
		}
		
		public int getPcLength() {
			return this.pc_length;
		}
		
		public void setPcLength(int pc_length) {
			this.pc_length = pc_length;
		}
		
		public String getName(ClassConstantPool constPool) {
			return constPool.get(this.nameIndex, ConstantPoolString.class).getString();
		}
		
		public int getNameIndex() {
			return this.nameIndex;
		}
		
		public void setNameIndex(int nameIndex) {
			this.nameIndex = nameIndex;
		}
		
		public String getDesc(ClassConstantPool constPool) {
			return constPool.get(this.descIndex, ConstantPoolString.class).getString();
		}
		
		public int getDescIndex() {
			return this.descIndex;
		}
		
		public void setDescIndex(int descIndex) {
			this.descIndex = descIndex;
		}
		
		public int getVariableIndex() {
			return this.variableIndex;
		}
		
		public void setVariableIndex(int variableIndex) {
			this.variableIndex = variableIndex;
		}
		
		public String toString(ClassConstantPool constPool) {
			String variableName = (constPool != null && constPool.size() >= this.nameIndex ?
					constPool.get(this.nameIndex).val(constPool) : "~UNKNOWN_VARIABLE_NAME");
			String variableDesc = (constPool != null && constPool.size() >= this.descIndex ?
					constPool.get(this.descIndex).val(constPool) : "~UNKNOWN_VARIABLE_DESC");
			return LocalVariable.class.getSimpleName() + ": {start_pc=" + this.start_pc + ", pc_length=" + this.pc_length
					+ ", name=" + variableName + ", desc=" + variableDesc
					+ ", variable_index=" + this.variableIndex + "}";
		}
	}
}
