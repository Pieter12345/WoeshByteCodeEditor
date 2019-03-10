package io.github.pieter12345.wbce;

import java.io.IOException;
import java.util.ArrayList;

import io.github.pieter12345.wbce.attribute.AttributeSet;

public class ClassFields {
	
	private ArrayList<ClassField> fields;
	
	private ClassFields(ArrayList<ClassField> fields) {
		this.fields = fields;
	}
	
	public static ClassFields fromInputStream(FancyInputStream inStream, ClassConstantPool constPool) throws IOException {
		int fieldCount = inStream.readTwoByteInt();
		ArrayList<ClassField> fields = new ArrayList<ClassField>(fieldCount);
		for(int i = 0; i < fieldCount; i++) {
			fields.add(ClassField.fromInputStream(inStream, constPool));
		}
		return new ClassFields(fields);
	}
	
	public byte[] toBytes() {
		@SuppressWarnings("resource")
		FancyByteArrayOutputStream outStream = new FancyByteArrayOutputStream();
		outStream.writeTwoByteInteger(this.fields.size());
		for(int i = 0; i < this.fields.size(); i++) {
			outStream.write(this.fields.get(i).toBytes());
		}
		return outStream.toByteArray();
	}
	
	public int size() {
		return this.fields.size();
	}
	
	public ClassField getField(int index) {
		return this.fields.get(index);
	}
	
	public ClassField[] getFields() {
		return this.fields.toArray(new ClassField[0]);
	}
	
	public void setField(int index, ClassField classField) {
		this.fields.set(index, classField);
	}
	
	public void addField(ClassField classField) {
		this.fields.add(classField);
	}
	
	public ClassField removeField(int index) {
		return this.fields.remove(index);
	}
	
	public boolean removeField(ClassField classField) {
		return this.fields.remove(classField);
	}
	
	public String toString(ClassConstantPool constPool) {
		String fieldStr = "ClassFields: {";
		for(int i = 0; i < fields.size(); i++) {
			ClassField field = fields.get(i);
			fieldStr += i + ": " + field.toString(constPool) + " ";
		}
		return fieldStr.trim() + "}";
	}
	
	public static class ClassField {
		private int accessFlags;
		private int fieldNameIndex;
		private int fieldDescIndex;
		private AttributeSet fieldAttributes;
		
		public ClassField(int accessFlags, int fieldNameIndex, int fieldDescIndex, AttributeSet fieldAttributes) {
			this.accessFlags = accessFlags;
			this.fieldNameIndex = fieldNameIndex;
			this.fieldDescIndex = fieldDescIndex;
			this.fieldAttributes = fieldAttributes;
		}
		
		public static ClassField fromInputStream(FancyInputStream inStream, ClassConstantPool constPool) throws IOException {
			int accessFlags = inStream.readTwoByteInt();
			int fieldNameIndex = inStream.readTwoByteInt();
			int fieldDescIndex = inStream.readTwoByteInt();
			AttributeSet fieldAttributes = AttributeSet.fromInputStream(inStream, constPool);
			return new ClassField(accessFlags, fieldNameIndex, fieldDescIndex, fieldAttributes);
		}
		
		public byte[] toBytes() {
			@SuppressWarnings("resource")
			FancyByteArrayOutputStream outStream = new FancyByteArrayOutputStream();
			outStream.writeTwoByteInteger(this.accessFlags);
			outStream.writeTwoByteInteger(this.fieldNameIndex);
			outStream.writeTwoByteInteger(this.fieldDescIndex);
			outStream.write(this.fieldAttributes.toBytes());
			return outStream.toByteArray();
		}
		
		public int getAccessFlags() {
			return this.accessFlags;
		}
		
		public void setAccessFlags(int accessFlags) {
			this.accessFlags = accessFlags;
		}
		
		public int getNameIndex() {
			return this.fieldNameIndex;
		}
		
		public void setNameIndex(int nameIndex) {
			this.fieldNameIndex = nameIndex;
		}
		
		public int getDescIndex() {
			return this.fieldDescIndex;
		}
		
		public void setDescIndex(int descIndex) {
			this.fieldDescIndex = descIndex;
		}
		
		public AttributeSet getAttributes() {
			return this.fieldAttributes;
		}
		
		public void setAttributes(AttributeSet attributes) {
			this.fieldAttributes = attributes;
		}
		
		public String getAccessFlagString() {
			boolean fieldPublic     = (accessFlags & 0x0001) == 0x0001;
			boolean fieldPrivate    = (accessFlags & 0x0002) == 0x0002;
			boolean fieldProtected  = (accessFlags & 0x0004) == 0x0004;
			boolean fieldStatic     = (accessFlags & 0x0008) == 0x0008;
			boolean fieldFinal      = (accessFlags & 0x0010) == 0x0010;
			boolean fieldVolatile   = (accessFlags & 0x0040) == 0x0040;
			boolean fieldTransient  = (accessFlags & 0x0080) == 0x0080;
			boolean fieldSynthetic  = (accessFlags & 0x1000) == 0x1000;
			boolean fieldEnum       = (accessFlags & 0x4000) == 0x4000;
			String accessFlagStr = "";
			if(fieldPublic) { accessFlagStr += "public "; }
			if(fieldPrivate) { accessFlagStr += "private "; }
			if(fieldProtected) { accessFlagStr += "protected "; }
			if(fieldStatic) { accessFlagStr += "static "; }
			if(fieldFinal) { accessFlagStr += "final "; }
			if(fieldVolatile) { accessFlagStr += "volatile "; }
			if(fieldTransient) { accessFlagStr += "transient "; }
			if(fieldSynthetic) { accessFlagStr += "synthetic "; }
			if(fieldEnum) { accessFlagStr += "enum "; }
			return accessFlagStr.trim();
		}
		
		public String getName(ClassConstantPool constPool) {
			return (constPool != null && constPool.hasIndex(this.fieldNameIndex) ?
					constPool.get(this.fieldNameIndex).val(constPool) : null);
		}
		
		public String toString(ClassConstantPool constPool) {
			String fieldName = (constPool != null && constPool.size() >= this.fieldNameIndex ?
					constPool.get(this.fieldNameIndex).val(constPool) : "~UNKNOWN_FIELD_NAME");
			String fieldDesc = (constPool != null && constPool.size() >= this.fieldDescIndex ?
					constPool.get(this.fieldDescIndex).val(constPool) : "~UNKNOWN_FIELD_DESC");
			
			return "ClassField: {" + this.getAccessFlagString() + " " + fieldName + " " + fieldDesc
					+ " (" + this.fieldAttributes.size() + " attributes)}";
		}
	}
	
}
