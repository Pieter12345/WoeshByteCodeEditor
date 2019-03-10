package io.github.pieter12345.wbce.decompile;

import java.util.ArrayList;

import io.github.pieter12345.wbce.ClassConstantPool;
import io.github.pieter12345.wbce.constantpool.ConstantPoolClassRef;
import io.github.pieter12345.wbce.constantpool.ConstantPoolMethodRef;
import io.github.pieter12345.wbce.constantpool.ConstantPoolNameAndTypeDesc;
import io.github.pieter12345.wbce.constantpool.ConstantPoolString;

public class MethodRefDecoder {

	private final String className;
	private final String methodName;
	private final String methodReturnTypeDesc;
	private final String[] methodArgsTypeDesc;
	private final String methodTypeDescRaw;
	
	public MethodRefDecoder(ClassConstantPool constPool, int methodDescIndex) throws DecompileException {
		try {
			ConstantPoolMethodRef methodRef = (ConstantPoolMethodRef) constPool.get(methodDescIndex, ConstantPoolMethodRef.class);
			
			ConstantPoolClassRef classRef = (ConstantPoolClassRef) constPool.get(methodRef.getClassIndex(), ConstantPoolClassRef.class);
			ConstantPoolNameAndTypeDesc methodNameAndTypeDesc =
					(ConstantPoolNameAndTypeDesc) constPool.get(methodRef.getNameAndTypeIndex(), ConstantPoolNameAndTypeDesc.class);
			
			ConstantPoolString className = (ConstantPoolString) constPool.get(classRef.getIndex(), ConstantPoolString.class);
	
			ConstantPoolString methodName = (ConstantPoolString) constPool.get(methodNameAndTypeDesc.getNameIndex(), ConstantPoolString.class);
			ConstantPoolString methodTypeDesc = (ConstantPoolString) constPool.get(methodNameAndTypeDesc.getTypeDescIndex(), ConstantPoolString.class);
			
			this.className = className.getString().replace('/', '.');
			this.methodName = methodName.getString();
			this.methodTypeDescRaw = methodTypeDesc.getString();
		} catch (ClassCastException | IndexOutOfBoundsException e) {
			throw new DecompileException("An Exception occured while getting a method reference from the constant pool.", e);
		}
		
		// Convert the method description.
		String methodDesc = this.methodTypeDescRaw;
		int splitIndex = methodDesc.indexOf(')');
		if(!methodDesc.startsWith("(") || splitIndex == -1 || splitIndex == methodDesc.length() - 1) {
			throw new DecompileException("Method descriptor not recognised: " + methodDesc);
		}
		
		String returnType = methodDesc.substring(splitIndex + 1, methodDesc.length());
		int arrayDim = 0;
		while(returnType.startsWith("[")) {
			returnType = returnType.substring(1);
			arrayDim++;
		}
		if(returnType.startsWith("L") && returnType.endsWith(";")) { // Ljava/lang/String;
			returnType = returnType.substring(1, returnType.length() - 1).replace('/', '.');
			if(returnType.startsWith("java.lang.")) {
				returnType = returnType.substring("java.lang.".length());
			}
		} else if(returnType.length() != 1) {
			throw new DecompileException("Method descriptor return type not recognised: " + returnType);
		} else {
			switch(returnType.charAt(0)) {
			case 'B': returnType = "byte"; break;
			case 'C': returnType = "char"; break;
			case 'D': returnType = "double"; break;
			case 'F': returnType = "float"; break;
			case 'I': returnType = "int"; break;
			case 'J': returnType = "long"; break;
			case 'S': returnType = "short"; break;
			case 'Z': returnType = "boolean"; break;
			case 'V': returnType = "void"; break;
			default: throw new DecompileException("Method descriptor return type not recognised: " + returnType);
			}
		}
		for(int dim = 0; dim < arrayDim; dim++) {
			returnType += "[]";
		}
		this.methodReturnTypeDesc = returnType;
		
		ArrayList<String> methodArgTypes = new ArrayList<String>();
		int index = 1; // Start at 1 to skip the '('.
		while(index < splitIndex) {
			
			arrayDim = 0;
			while(methodDesc.startsWith("[", index)) {
				index++;
				arrayDim++;
			}
			
			String methodArg = "";
			switch(methodDesc.charAt(index)) {
			case 'L':
				int closingIndex = methodDesc.indexOf(';', index);
				if(closingIndex == -1) {
					throw new DecompileException("Method descriptor argument type not recognised in: " + methodDesc + " at index: " + index);
				}
				methodArg = methodDesc.substring(index + 1, closingIndex).replace('/', '.');
				index += methodArg.length() + 1; // +1 for the ';'.
				if(methodArg.startsWith("java.lang.")) {
					methodArg = methodArg.substring("java.lang.".length());
				}
				break;
			case 'B': methodArg = "byte"; break;
			case 'C': methodArg = "char"; break;
			case 'D': methodArg = "double"; break;
			case 'F': methodArg = "float"; break;
			case 'I': methodArg = "int"; break;
			case 'J': methodArg = "long"; break;
			case 'S': methodArg = "short"; break;
			case 'Z': methodArg = "boolean"; break;
			default: throw new DecompileException("Method descriptor argument type not recognised in: " + methodDesc + " at index: " + index);
			}
			index++;
			
			for(int dim = 0; dim < arrayDim; dim++) {
				methodArg += "[]";
			}
			
			methodArgTypes.add(methodArg);
		}
		this.methodArgsTypeDesc = methodArgTypes.toArray(new String[0]);
		
	}
	
	/**
	 * getClassName method.
	 * @return The class containing the method. Format: "my.package.MyClass".
	 */
	public String getClassName() {
		return this.className;
	}
	
	/**
	 * getMethodName method.
	 * @return The name of the method.
	 */
	public String getMethodName() {
		return this.methodName;
	}
	
	/**
	 * getMethodReturnTypeDesc method.
	 * @return The readible version of the method return type description. Example: "some.package.SomeObject[][]" or "int".
	 */
	public String getMethodReturnTypeDesc() {
		return this.methodReturnTypeDesc;
	}
	
	/**
	 * getMethodArgsTypeDesc method.
	 * @return The readible version of the method argument type description. Example: {"int", "some.package.SomeObj[][]"}.
	 */
	public String[] getMethodArgsTypeDesc() {
		return this.methodArgsTypeDesc;
	}
	
	/**
	 * getRawMethodTypeDesc method.
	 * @return The raw method type description. Example: "(I[[Lsome/package/SomeObject;)V".
	 */
	public String getRawMethodTypeDesc() {
		return this.methodTypeDescRaw;
	}
	
	/**
	 * getMethodArgSize method.
	 * @return The amount of arguments that have to be passed to the method.
	 */
	public int getMethodArgSize() {
		return this.methodArgsTypeDesc.length;
	}
	
}
