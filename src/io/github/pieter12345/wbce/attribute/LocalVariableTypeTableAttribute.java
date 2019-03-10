package io.github.pieter12345.wbce.attribute;

import java.io.IOException;
import java.util.ArrayList;

import com.sun.org.apache.bcel.internal.classfile.LocalVariable;

import io.github.pieter12345.wbce.ClassConstantPool;
import io.github.pieter12345.wbce.FancyByteArrayOutputStream;
import io.github.pieter12345.wbce.FancyInputStream;

public class LocalVariableTypeTableAttribute implements IAttribute {
	
	// Variables & Constants.
	private int attrNameIndex; // Index of the "LocalVariableTypeTable" string in the constant pool.
	private ArrayList<LocalVariableType> localVariableTypeTable;
	
	public LocalVariableTypeTableAttribute(int attrNameIndex, ArrayList<LocalVariableType> localVariableTypeTable) {
		this.attrNameIndex = attrNameIndex;
		this.localVariableTypeTable = localVariableTypeTable;
	}
	
	public static LocalVariableTypeTableAttribute fromInputStream(int attrNameIndex, FancyInputStream inStream) throws IOException {
		inStream.readFourByteInt(); // attrInfoLength.
		int localVariableTypeCount = inStream.readTwoByteInt();
		ArrayList<LocalVariableType> localVariableTypeTable = new ArrayList<LocalVariableType>(localVariableTypeCount);
		for(int i = 0; i < localVariableTypeCount; i++) {
			localVariableTypeTable.add(LocalVariableType.fromInputStream(inStream));
		}
		return new LocalVariableTypeTableAttribute(attrNameIndex, localVariableTypeTable);
	}
	
	@Override
	public byte[] toBytes() {
		@SuppressWarnings("resource")
		FancyByteArrayOutputStream outStream = new FancyByteArrayOutputStream();
		outStream.writeTwoByteInteger(this.localVariableTypeTable.size());
		for(int i = 0; i < this.localVariableTypeTable.size(); i++) {
			outStream.write(this.localVariableTypeTable.get(i).toBytes());
		}
		byte[] outStreamBytes = outStream.toByteArray();
		
		@SuppressWarnings("resource")
		FancyByteArrayOutputStream outStream2 = new FancyByteArrayOutputStream();
		outStream2.writeTwoByteInteger(this.attrNameIndex);
		outStream2.writeFourByteInteger(outStreamBytes.length);
		outStream2.write(outStreamBytes);
		
		return outStream2.toByteArray();
	}
	
	public LocalVariableType getLocalVariableType(int index) {
		return this.localVariableTypeTable.get(index);
	}
	
	public void setLocalVariableType(int index, LocalVariableType localVariableType) {
		this.localVariableTypeTable.set(index, localVariableType);
	}
	
	public void addLocalVariableType(LocalVariableType localVariableType) {
		this.localVariableTypeTable.add(localVariableType);
	}
	
	public LocalVariableType removeLocalVariableType(int index) {
		return this.localVariableTypeTable.remove(index);
	}
	
	public boolean removeLocalVariableType(LocalVariableType localVariableType) {
		return this.localVariableTypeTable.remove(localVariableType);
	}
	
	@Override
	public String toString(ClassConstantPool constPool) {
		String localVariableTypeTableStr = "";
		for(int i = 0; i < this.localVariableTypeTable.size(); i++) {
			localVariableTypeTableStr += i + ": " + this.localVariableTypeTable.get(i).toString(constPool) + " ";
		}
		return LocalVariableTypeTableAttribute.class.getSimpleName() + ": {" + localVariableTypeTableStr.trim() + "}";
	}
	
	public static class LocalVariableType {
		
		// Variables & Constants.
		private int start_pc; // The instruction/line at which the variable is defined.
		private int pc_length; // The amount of instructions/lines for which the variable is defined.
		private int nameIndex;
		private int signatureIndex;
		private int variableIndex; // Index of the variable in the local variable array. Double/Long's also use index+1.
		
		public LocalVariableType(int start_pc, int pc_length, int nameIndex, int descIndex, int variableIndex) {
			this.start_pc = start_pc;
			this.pc_length = pc_length;
			this.nameIndex = nameIndex;
			this.signatureIndex = descIndex;
			this.variableIndex = variableIndex;
		}
		
		public static LocalVariableType fromInputStream(FancyInputStream inStream) throws IOException {
			int start_pc = inStream.readTwoByteInt();
			int pc_length = inStream.readTwoByteInt();
			int nameIndex = inStream.readTwoByteInt();
			int signatureIndex = inStream.readTwoByteInt();
			int variableIndex = inStream.readTwoByteInt();
			return new LocalVariableType(start_pc, pc_length, nameIndex, signatureIndex, variableIndex);
		}
		
		public byte[] toBytes() {
			@SuppressWarnings("resource")
			FancyByteArrayOutputStream outStream = new FancyByteArrayOutputStream();
			outStream.writeTwoByteInteger(this.start_pc);
			outStream.writeTwoByteInteger(this.pc_length);
			outStream.writeTwoByteInteger(this.nameIndex);
			outStream.writeTwoByteInteger(this.signatureIndex);
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
		
		public int getNameIndex() {
			return this.nameIndex;
		}
		
		public void setNameIndex(int nameIndex) {
			this.nameIndex = nameIndex;
		}
		
		public int getSignatureIndex() {
			return this.signatureIndex;
		}
		
		public void setSignatureIndex(int descIndex) {
			this.signatureIndex = descIndex;
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
			String variableSignature = (constPool != null && constPool.size() >= this.signatureIndex ?
					constPool.get(this.signatureIndex).val(constPool) : "~UNKNOWN_VARIABLE_SIGNATURE");
			return LocalVariable.class.getSimpleName() + ": {"
					+ "start_pc=" + this.start_pc
					+ ", pc_length=" + this.pc_length
					+ ", name_index=" + this.nameIndex + "->" + variableName
					+ ", signature_index=" + this.signatureIndex + "->" + variableSignature
					+ ", variable_index=" + this.variableIndex + "}";
		}
	}
}
