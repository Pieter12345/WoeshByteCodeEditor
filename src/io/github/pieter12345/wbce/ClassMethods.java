package io.github.pieter12345.wbce;

import java.io.IOException;
import java.util.ArrayList;

import io.github.pieter12345.wbce.attribute.AttributeSet;
import io.github.pieter12345.wbce.constantpool.ConstantPoolString;

public class ClassMethods {
	
	private ArrayList<ClassMethod> methods;
	
	private ClassMethods(ArrayList<ClassMethod> methods) {
		this.methods = methods;
	}
	
	public static ClassMethods fromInputStream(FancyInputStream inStream, ClassConstantPool constPool) throws IOException {
		int methodCount = inStream.readTwoByteInt();
		ArrayList<ClassMethod> methods = new ArrayList<ClassMethod>(methodCount);
		for(int i = 0; i < methodCount; i++) {
			methods.add(ClassMethod.fromInputStream(inStream, constPool));
		}
		return new ClassMethods(methods);
	}
	
	public byte[] toBytes() {
		@SuppressWarnings("resource")
		FancyByteArrayOutputStream outStream = new FancyByteArrayOutputStream();
		outStream.writeTwoByteInteger(this.methods.size());
		for(int i = 0; i < this.methods.size(); i++) {
			outStream.write(this.methods.get(i).toBytes());
		}
		return outStream.toByteArray();
	}
	
	public int size() {
		return this.methods.size();
	}
	
	public ClassMethod getMethod(int index) {
		return this.methods.get(index);
	}
	
	public ClassMethod getMethod(String name, String descriptor, ClassConstantPool constPool) {
		for(int i = 0; i < this.methods.size(); i++) {
			ClassMethod method = this.methods.get(i);
			if(constPool.get(method.getNameIndex()).val(constPool).equals(name) && constPool.get(method.getDescIndex()).val(constPool).equals(descriptor)) {
				return method;
			}
		}
		return null;
	}
	
	public ClassMethod[] getMethods() {
		return this.methods.toArray(new ClassMethod[0]);
	}
	
	public void setMethod(int index, ClassMethod classMethod) {
		this.methods.set(index, classMethod);
	}
	
	public void addMethod(ClassMethod classMethod) {
		this.methods.add(classMethod);
	}
	
	public ClassMethod removeMethod(int index) {
		return this.methods.remove(index);
	}
	
	public boolean removeMethod(ClassMethod classMethod) {
		return this.methods.remove(classMethod);
	}
	
	public String toString(ClassConstantPool constPool) {
		String methodStr = "ClassMethods: {";
		for(int i = 0; i < methods.size(); i++) {
			ClassMethod method = methods.get(i);
			methodStr += i + ": " + method.toString(constPool) + " ";
		}
		return methodStr.trim() + "}";
	}
	
	public static class ClassMethod {
		private MethodAccessFlags accessFlags;
		private int methodNameIndex;
		private int methodDescIndex;
		private AttributeSet methodAttributes;
		
		public ClassMethod(MethodAccessFlags accessFlags, int methodNameIndex, int methodDescIndex, AttributeSet methodAttributes) {
			this.accessFlags = accessFlags;
			this.methodNameIndex = methodNameIndex;
			this.methodDescIndex = methodDescIndex;
			this.methodAttributes = methodAttributes;
		}
		
		public static ClassMethod fromInputStream(FancyInputStream inStream, ClassConstantPool constPool) throws IOException {
			MethodAccessFlags accessFlags = new MethodAccessFlags(inStream.readTwoByteInt());
			int methodNameIndex = inStream.readTwoByteInt();
			int methodDescIndex = inStream.readTwoByteInt();
			AttributeSet methodAttributes = AttributeSet.fromInputStream(inStream, constPool);
			return new ClassMethod(accessFlags, methodNameIndex, methodDescIndex, methodAttributes);
		}
		
		public byte[] toBytes() {
			@SuppressWarnings("resource")
			FancyByteArrayOutputStream outStream = new FancyByteArrayOutputStream();
			outStream.writeTwoByteInteger(this.accessFlags.getValue());
			outStream.writeTwoByteInteger(this.methodNameIndex);
			outStream.writeTwoByteInteger(this.methodDescIndex);
			outStream.write(this.methodAttributes.toBytes());
			return outStream.toByteArray();
		}
		
		public MethodAccessFlags getAccessFlags() {
			return this.accessFlags;
		}
		
		public void setAccessFlags(MethodAccessFlags accessFlags) {
			this.accessFlags = accessFlags;
		}
		
		public String getName(ClassConstantPool constPool) {
			return constPool.get(this.methodNameIndex, ConstantPoolString.class).getString();
		}
		
		public int getNameIndex() {
			return this.methodNameIndex;
		}
		
		public void setNameIndex(int nameIndex) {
			this.methodNameIndex = nameIndex;
		}
		
		public String getDesc(ClassConstantPool constPool) {
			return constPool.get(this.methodDescIndex, ConstantPoolString.class).getString();
		}
		
		public int getDescIndex() {
			return this.methodDescIndex;
		}
		
		public void setDescIndex(int descIndex) {
			this.methodDescIndex = descIndex;
		}
		
		public AttributeSet getAttributes() {
			return this.methodAttributes;
		}
		
		public void setAttributes(AttributeSet attributes) {
			this.methodAttributes = attributes;
		}
		
		public String toString(ClassConstantPool constPool) {
			String methodName = (constPool != null && constPool.size() >= this.methodNameIndex ?
					constPool.get(this.methodNameIndex).val(constPool) : "~UNKNOWN_METHOD_NAME");
			String methodDesc = (constPool != null && constPool.size() >= this.methodDescIndex ?
					constPool.get(this.methodDescIndex).val(constPool) : "~UNKNOWN_METHOD_DESC");
			
			return "ClassMethod: {" + this.getAccessFlags().toCodeString() + " " + methodName + " " + methodDesc
					+ " (" + this.methodAttributes.size() + " attributes)}";
		}
	}
	
}
