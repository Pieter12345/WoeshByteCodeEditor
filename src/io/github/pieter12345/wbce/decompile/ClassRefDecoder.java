package io.github.pieter12345.wbce.decompile;

import io.github.pieter12345.wbce.ClassConstantPool;
import io.github.pieter12345.wbce.constantpool.ConstantPoolClassRef;
import io.github.pieter12345.wbce.constantpool.ConstantPoolString;

public class ClassRefDecoder {
	
	private final String className;
	private final String rawClassName;
	private final int arrayDim;
	
	public ClassRefDecoder(ClassConstantPool constPool, int classDescIndex) throws DecompileException {
		try {
			ConstantPoolClassRef classRef = (ConstantPoolClassRef) constPool.get(classDescIndex, ConstantPoolClassRef.class);
			
			ConstantPoolString className = (ConstantPoolString) constPool.get(classRef.getIndex(), ConstantPoolString.class);
			this.rawClassName = className.getString();
		} catch (ClassCastException | IndexOutOfBoundsException e) {
			throw new DecompileException("An Exception occured while getting a class reference from the constant pool.", e);
		}
		
		// Convert the field type description.
		String className = this.rawClassName;
		int arrayDim = 0;
		while(className.startsWith("[")) {
			className = className.substring(1);
			arrayDim++;
		}
		if(arrayDim == 0) { // "java/lang/String".
			className = className.replace('/', '.');
			if(className.startsWith("java.lang.")) {
				className = className.substring("java.lang.".length());
			}
		} else if(className.startsWith("L") && className.endsWith(";")) { // "Ljava/lang/String;".
			className = className.substring(1, className.length() - 1).replace('/', '.');
			if(className.startsWith("java.lang.")) {
				className = className.substring("java.lang.".length());
			}
		} else if(className.length() != 1) {
			throw new DecompileException("Class descriptor not recognised: " + this.rawClassName);
		} else {
			switch(className.charAt(0)) {
			case 'B': className = "byte"; break;
			case 'C': className = "char"; break;
			case 'D': className = "double"; break;
			case 'F': className = "float"; break;
			case 'I': className = "int"; break;
			case 'J': className = "long"; break;
			case 'S': className = "short"; break;
			case 'Z': className = "boolean"; break;
			default: throw new DecompileException("Class descriptor not recognised: " + this.rawClassName);
			}
		}
		for(int dim = 0; dim < arrayDim; dim++) {
			className += "[]";
		}
		this.className = className;
		this.arrayDim = arrayDim;
		
	}
	
	/**
	 * getClassName class.
	 * @return The name of the class. Format: "my.package.MyClass[][]".
	 */
	public String getClassName() {
		return this.className;
	}
	
	/**
	 * getClassName class.
	 * @return The name of the class. Format: "[[Lmy/package/MyClass;".
	 */
	public String getRawClassName() {
		return this.rawClassName;
	}
	
	/**
	 * getArrayDim class.
	 * @return The array dimension of the class. Example: "[[Lmy/package/MyClass;" returns 2 and "Lmy/package/MyClass;" returns 0.
	 */
	public int getArrayDim() {
		return this.arrayDim;
	}
	
}
