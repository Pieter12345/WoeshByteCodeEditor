package io.github.pieter12345.wbce.decompile;

import io.github.pieter12345.wbce.ClassConstantPool;
import io.github.pieter12345.wbce.constantpool.ConstantPoolClassRef;
import io.github.pieter12345.wbce.constantpool.ConstantPoolFieldRef;
import io.github.pieter12345.wbce.constantpool.ConstantPoolNameAndTypeDesc;
import io.github.pieter12345.wbce.constantpool.ConstantPoolString;

public class FieldRefDecoder {

	private final String className;
	private final String fieldName;
	private final String fieldTypeDesc;
	private final String fieldTypeDescRaw;
	
	public FieldRefDecoder(ClassConstantPool constPool, int fieldDescIndex) throws DecompileException {
		try {
			ConstantPoolFieldRef fieldRef = (ConstantPoolFieldRef) constPool.get(fieldDescIndex, ConstantPoolFieldRef.class);
			
			ConstantPoolClassRef classRef = (ConstantPoolClassRef) constPool.get(fieldRef.getClassIndex(), ConstantPoolClassRef.class);
			ConstantPoolNameAndTypeDesc fieldNameAndTypeDesc =
					(ConstantPoolNameAndTypeDesc) constPool.get(fieldRef.getNameAndTypeIndex(), ConstantPoolNameAndTypeDesc.class);
			
			ConstantPoolString className = (ConstantPoolString) constPool.get(classRef.getIndex(), ConstantPoolString.class);
			
			ConstantPoolString fieldName = (ConstantPoolString) constPool.get(fieldNameAndTypeDesc.getNameIndex(), ConstantPoolString.class);
			ConstantPoolString fieldTypeDesc = (ConstantPoolString) constPool.get(fieldNameAndTypeDesc.getTypeDescIndex(), ConstantPoolString.class);
			
			String classNameStr = className.getString().replace('/', '.');
			if(classNameStr.startsWith("java.lang.")) {
				classNameStr = classNameStr.substring("java.lang.".length());
			}
			
			this.className = classNameStr;
			this.fieldName = fieldName.getString();
			this.fieldTypeDescRaw = fieldTypeDesc.getString();
		} catch (ClassCastException | IndexOutOfBoundsException e) {
			throw new DecompileException("An Exception occured while getting a field reference from the constant pool.", e);
		}
		
		// Convert the field type description.
		String typeDesc = this.fieldTypeDescRaw;
		int arrayDim = 0;
		while(typeDesc.startsWith("[")) {
			typeDesc = typeDesc.substring(1);
			arrayDim++;
		}
		if(typeDesc.startsWith("L") && typeDesc.endsWith(";")) { // Ljava/lang/String;
			typeDesc = typeDesc.substring(1, typeDesc.length() - 1).replace('/', '.');
			if(typeDesc.startsWith("java.lang.")) {
				typeDesc = typeDesc.substring("java.lang.".length());
			}
		} else if(typeDesc.length() != 1) {
			throw new DecompileException("Field descriptor not recognised: " + this.fieldTypeDescRaw);
		} else {
			switch(typeDesc.charAt(0)) {
			case 'B': typeDesc = "byte"; break;
			case 'C': typeDesc = "char"; break;
			case 'D': typeDesc = "double"; break;
			case 'F': typeDesc = "float"; break;
			case 'I': typeDesc = "int"; break;
			case 'J': typeDesc = "long"; break;
			case 'S': typeDesc = "short"; break;
			case 'Z': typeDesc = "boolean"; break;
			default: throw new DecompileException("Field descriptor not recognised: " + this.fieldTypeDescRaw);
			}
		}
		for(int dim = 0; dim < arrayDim; dim++) {
			typeDesc += "[]";
		}
		this.fieldTypeDesc = typeDesc;
		
	}
	
	/**
	 * getClassName method.
	 * @return The class containing the field. Format: "my.package.MyClass".
	 */
	public String getClassName() {
		return this.className;
	}
	
	/**
	 * getFieldName method.
	 * @return The name of the field.
	 */
	public String getFieldName() {
		return this.fieldName;
	}
	
	/**
	 * getFieldTypeDesc method.
	 * @return The readible version of the field type description. Example: "some.package.SomeObject[][]" or "int".
	 */
	public String getFieldTypeDesc() {
		return this.fieldTypeDesc;
	}
	
	/**
	 * getRawFieldTypeDesc method.
	 * @return The raw field type description. Example: "[[Lsome/package/SomeObject;" or "I".
	 */
	public String getRawFieldTypeDesc() {
		return this.fieldTypeDescRaw;
	}
	
}
