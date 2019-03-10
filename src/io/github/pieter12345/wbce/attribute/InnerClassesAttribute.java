package io.github.pieter12345.wbce.attribute;

import java.io.IOException;
import java.util.ArrayList;

import io.github.pieter12345.wbce.ClassConstantPool;
import io.github.pieter12345.wbce.FancyByteArrayOutputStream;
import io.github.pieter12345.wbce.FancyInputStream;

public class InnerClassesAttribute implements IAttribute {
	
	// Variables & Constants.
	private int attrNameIndex; // Index of the "InnerClasses" string in the constant pool.
	private ArrayList<InnerClass> innerClasses;
	
	public InnerClassesAttribute(int attrNameIndex, ArrayList<InnerClass> innerClasses) {
		this.attrNameIndex = attrNameIndex;
		this.innerClasses = innerClasses;
	}
	
	public static InnerClassesAttribute fromInputStream(int attrNameIndex, FancyInputStream inStream) throws IOException {
		inStream.readFourByteInt(); // attrInfoLength.
		int innerClassCount = inStream.readTwoByteInt();
		ArrayList<InnerClass> innerClasses = new ArrayList<InnerClass>(innerClassCount);
		for(int i = 0; i < innerClassCount; i++) {
			innerClasses.add(InnerClass.fromInputStream(inStream));
		}
		return new InnerClassesAttribute(attrNameIndex, innerClasses);
	}
	
	@Override
	public byte[] toBytes() {
		@SuppressWarnings("resource")
		FancyByteArrayOutputStream outStream = new FancyByteArrayOutputStream();
		outStream.writeTwoByteInteger(this.innerClasses.size());
		for(int i = 0; i < this.innerClasses.size(); i++) {
			outStream.write(this.innerClasses.get(i).toBytes());
		}
		byte[] outStreamBytes = outStream.toByteArray();
		
		@SuppressWarnings("resource")
		FancyByteArrayOutputStream outStream2 = new FancyByteArrayOutputStream();
		outStream2.writeTwoByteInteger(this.attrNameIndex);
		outStream2.writeFourByteInteger(outStreamBytes.length);
		outStream2.write(outStreamBytes);
		
		return outStream2.toByteArray();
	}
	
	public InnerClass getInnerClass(int index) {
		return this.innerClasses.get(index);
	}
	
	public void setInnerClass(int index, InnerClass innerClass) {
		this.innerClasses.set(index, innerClass);
	}
	
	public void addInnerClass(InnerClass innerClass) {
		this.innerClasses.add(innerClass);
	}
	
	public InnerClass removeInnerClass(int index) {
		return this.innerClasses.remove(index);
	}
	
	public boolean removeInnerClass(InnerClass innerClass) {
		return this.innerClasses.remove(innerClass);
	}
	
	@Override
	public String toString(ClassConstantPool constPool) {
		String innerClassesStr = "";
		for(int i = 0; i < this.innerClasses.size(); i++) {
			innerClassesStr += i + ": " + this.innerClasses.get(i).toString(constPool) + " ";
		}
		return InnerClassesAttribute.class.getSimpleName() + ": {" + innerClassesStr.trim() + "}";
	}
	
	public static class InnerClass {
		
		// Variables & Constants.
		private int innerClassInfoIndex;
		private int outerClassInfoIndex;
		private int innerNameIndex;
		private int innerClassAccessFlags;
		
		public InnerClass(int innerClassInfoIndex, int outerClassInfoIndex, int innerNameIndex, int innerClassAccessFlags) {
			this.innerClassInfoIndex = innerClassInfoIndex;
			this.outerClassInfoIndex = outerClassInfoIndex;
			this.innerNameIndex = innerNameIndex;
			this.innerClassAccessFlags = innerClassAccessFlags;
		}
		
		public static InnerClass fromInputStream(FancyInputStream inStream) throws IOException {
			int innerClassInfoIndex = inStream.readTwoByteInt();
			int outerClassInfoIndex = inStream.readTwoByteInt();
			int innerNameIndex = inStream.readTwoByteInt();
			int innerClassAccessFlags = inStream.readTwoByteInt();
			return new InnerClass(innerClassInfoIndex, outerClassInfoIndex, innerNameIndex, innerClassAccessFlags);
		}
		
		public byte[] toBytes() {
			@SuppressWarnings("resource")
			FancyByteArrayOutputStream outStream = new FancyByteArrayOutputStream();
			outStream.writeTwoByteInteger(this.innerClassInfoIndex);
			outStream.writeTwoByteInteger(this.outerClassInfoIndex);
			outStream.writeTwoByteInteger(this.innerNameIndex);
			outStream.writeTwoByteInteger(this.innerClassAccessFlags);
			return outStream.toByteArray();
		}
		
		public int getInnerClassInfoIndex() {
			return this.innerClassInfoIndex;
		}
		
		public void setInnerClassInfoIndex(int innerClassInfoIndex) {
			this.innerClassInfoIndex = innerClassInfoIndex;
		}
		
		public int getOuterClassInfoIndex() {
			return this.outerClassInfoIndex;
		}
		
		public void setOuterClassInfoIndex(int outerClassInfoIndex) {
			this.outerClassInfoIndex = outerClassInfoIndex;
		}
		
		public int getInnerNameIndex() {
			return this.innerNameIndex;
		}
		
		public void setInnerNameIndex(int innerNameIndex) {
			this.innerNameIndex = innerNameIndex;
		}
		
		public int getInnerClassAccessFlags() {
			return this.innerClassAccessFlags;
		}
		
		public void setInnerClassAccessFlags(int innerClassAccessFlags) {
			this.innerClassAccessFlags = innerClassAccessFlags;
		}
		
		public String toString(ClassConstantPool constPool) {
			String innerClassInfo = (constPool != null && constPool.size() >= this.innerClassInfoIndex ?
					constPool.get(this.innerClassInfoIndex).val(constPool) : "~UNKNOWN_INNER_CLASS_INFO");
			String outerClassInfo = (constPool != null && constPool.size() >= this.outerClassInfoIndex ?
					constPool.get(this.outerClassInfoIndex).val(constPool) : "~UNKNOWN_OUTER_CLASS_INFO");
			String innerName = (constPool != null && constPool.size() >= this.innerNameIndex ?
					constPool.get(this.innerNameIndex).val(constPool) : "~UNKNOWN_INNER_NAME");
			return InnerClass.class.getSimpleName() + ": {"
					+ "inner_class_info_index=" + this.innerClassInfoIndex + "->" + innerClassInfo
					+ ", outer_class_info_index=" + this.outerClassInfoIndex + "->" + outerClassInfo
					+ ", inner_name_index=" + this.innerNameIndex + "->" + innerName
					+ ", inner_class_access_flags=" + this.innerClassAccessFlags + "}";
		}
	}
}
