package io.github.pieter12345.wbce;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import io.github.pieter12345.wbce.ClassFields.ClassField;
import io.github.pieter12345.wbce.ClassInterfaces.ClassInterface;
import io.github.pieter12345.wbce.ClassMethods.ClassMethod;
import io.github.pieter12345.wbce.constantpool.ConstantPoolObject;
import io.github.pieter12345.wbce.constantpool.ConstantPoolString;

public class Decompiler {
	
	// Variables & Constants.
	private final ClassFile classFile;
	
	
	public Decompiler(ClassFile classFile) {
		this.classFile = classFile;
	}
	
	public void decompile(File outputFile) throws DecompileException, IOException {
		
		// Check if the class file has been read.
		if(!this.classFile.isRead()) {
			throw new DecompileException("ClassFile has not been read yet (use ClassFile's readFile() method before decompiling).");
		}
		
		// Initialize the decompile string.
		String decompStr = "";
		String indentation = ""; // For TAB's.
		
		// Add the package if the class is in a package.
		String thisIdentifier = this.classFile.getThisClassIdentifier();
		if(thisIdentifier.contains("/")) {
			String packageStr = thisIdentifier.replace('/', '.').replaceAll("\\.[^\\.]$", "");
			decompStr += packageStr + "\n\n";
		}
		
		// Create the "public abstract class ThisClass extends A implements B, C, D { ...".
		int classAccessFlags = this.classFile.getAccessFlags();
		String className = thisIdentifier.substring(thisIdentifier.lastIndexOf('.') + 1);
		
		String accessFlagStr = "";
		boolean classPublic     = (classAccessFlags & 0x0001) == 0x0001;
		boolean classFinal      = (classAccessFlags & 0x0010) == 0x0010;
		boolean classSuper      = (classAccessFlags & 0x0020) == 0x0020;
		boolean classInterface  = (classAccessFlags & 0x0200) == 0x0200;
		boolean classAbstract   = (classAccessFlags & 0x0400) == 0x0400;
		boolean classSynthetic  = (classAccessFlags & 0x1000) == 0x1000;
		boolean classAnnotation = (classAccessFlags & 0x2000) == 0x2000;
		boolean classEnum       = (classAccessFlags & 0x4000) == 0x4000;
		if(classPublic) { accessFlagStr += "public "; }
		if(classFinal) { accessFlagStr += "final "; }
		if(classInterface) { accessFlagStr += "interface "; }
		if(classAbstract) { accessFlagStr += "abstract "; }
		if(classSynthetic) { accessFlagStr += "synthetic "; }
		if(classSuper) { accessFlagStr += "class "; } // If it's a superclass, it's a normal "class".
		if(classAnnotation) { accessFlagStr += "annotation "; }
		if(classEnum) { accessFlagStr += "enum "; }
		
		String extendsStr = "";
		String superIdentifier = this.classFile.getSuperClassIdentifier().replace('/', '.');
		if(!superIdentifier.equals("java.lang.Object")) {
			extendsStr += " extends " + superIdentifier;
		}
		
		String implementsStr = "";
		ClassInterface[] interfaces = this.classFile.getInterfaces().getInterfaces();
		if(interfaces.length > 0) {
			implementsStr += " implements " + interfaces[0];
			for(int i = 1; i < interfaces.length; i++) {
				implementsStr += ", " + interfaces[i];
			}
		}
		
		decompStr += indentation + accessFlagStr + className + extendsStr + implementsStr + " {\n";
		indentation += "\t";
		decompStr += indentation + "\n";
		
		// Create class Fields.
		ClassConstantPool constPool = this.classFile.getConstantPool();
		ClassField[] fields = this.classFile.getFields().getFields();
		if(fields.length > 0) {
			for(ClassField field : fields) {
				
				// Get the access flags.
				String fieldAccessFlagStr = field.getAccessFlagString();
				
				// Get the field name.
				if(!constPool.hasIndex(field.getNameIndex())) {
					throw new DecompileException("Field name index is not a valid index in the constant pool: " + field.getNameIndex());
				}
				ConstantPoolObject nameObj = constPool.get(field.getNameIndex());
				if(!(nameObj instanceof ConstantPoolString)) {
					throw new DecompileException("Field name index is expected to point at "
							+ ConstantPoolString.class.getSimpleName() + " but found " + nameObj.getClass().getSimpleName() + ".");
				}
				String fieldName = ((ConstantPoolString) nameObj).getString();
				
				// Get the field description.
				if(!constPool.hasIndex(field.getDescIndex())) {
					throw new DecompileException("Field descriptor index is not a valid index in the constant pool: " + field.getDescIndex());
				}
				ConstantPoolObject descObj = constPool.get(field.getDescIndex());
				if(!(descObj instanceof ConstantPoolString)) {
					throw new DecompileException("Field descriptor index is expected to point at "
							+ ConstantPoolString.class.getSimpleName() + " but found " + descObj.getClass().getSimpleName() + ".");
				}
				String fieldDesc = ((ConstantPoolString) descObj).getString();
				
				// Convert the field description.
				int arrayDim = 0;
				while(fieldDesc.startsWith("[")) {
					fieldDesc = fieldDesc.substring(1);
					arrayDim++;
				}
				if(fieldDesc.startsWith("L") && fieldDesc.endsWith(";")) { // Ljava/lang/String;
					fieldDesc = fieldDesc.substring(1, fieldDesc.length() - 1).replace('/', '.');
				} else if(fieldDesc.length() != 1) {
					throw new DecompileException("Field descriptor not recognised: " + fieldDesc);
				} else {
					switch(fieldDesc.charAt(0)) {
					case 'B': fieldDesc = "byte"; break;
					case 'C': fieldDesc = "char"; break;
					case 'D': fieldDesc = "double"; break;
					case 'F': fieldDesc = "float"; break;
					case 'I': fieldDesc = "int"; break;
					case 'J': fieldDesc = "long"; break;
					case 'S': fieldDesc = "short"; break;
					case 'Z': fieldDesc = "boolean"; break;
					default: throw new DecompileException("Field descriptor not recognised: " + fieldDesc);
					}
				}
				for(int dim = 0; dim < arrayDim; dim++) {
					fieldDesc += "[]";
				}
				
				// Add the line.
				decompStr += indentation + fieldAccessFlagStr + " " + fieldDesc + " " + fieldName + ";\n";
				
			}
			decompStr += indentation + "\n";
		}
		
		// Create class methods.
		ClassMethod[] methods = this.classFile.getMethods().getMethods();
		if(methods.length > 0) {
			for(ClassMethod method : methods) {
				
				// Get the access flags.
				String methodAccessFlagStr = method.getAccessFlagString();
				
				// Get the method name.
				if(!constPool.hasIndex(method.getNameIndex())) {
					throw new DecompileException("Method name index is not a valid index in the constant pool: " + method.getNameIndex());
				}
				ConstantPoolObject nameObj = constPool.get(method.getNameIndex());
				if(!(nameObj instanceof ConstantPoolString)) {
					throw new DecompileException("Method name index is expected to point at "
							+ ConstantPoolString.class.getSimpleName() + " but found " + nameObj.getClass().getSimpleName() + ".");
				}
				String methodName = ((ConstantPoolString) nameObj).getString();
				
				// Get the method description.
				if(!constPool.hasIndex(method.getDescIndex())) {
					throw new DecompileException("Method descriptor index is not a valid index in the constant pool: " + method.getDescIndex());
				}
				ConstantPoolObject descObj = constPool.get(method.getDescIndex());
				if(!(descObj instanceof ConstantPoolString)) {
					throw new DecompileException("Method descriptor index is expected to point at "
							+ ConstantPoolString.class.getSimpleName() + " but found " + descObj.getClass().getSimpleName() + ".");
				}
				String methodDesc = ((ConstantPoolString) descObj).getString();
				
				// Check if the method is a constructor.
				boolean isConstructor = methodName.equals("<init>");
					
				// Convert the method description.
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
				
				String methodArgsStr = "";
				String[] methodArgs = methodDesc.substring(1, splitIndex).split("(?<=(^|       |;[^\\[]{0,65535}))"); // TODO - Fix regex.
				for(String methodArg : methodArgs) {
					String methodArgStr = methodArg;
					
					
					
					
					methodArgsStr += (methodArgsStr.isEmpty() ? "" : ", ") + methodArgStr;
					System.out.println(methodArg);
				}
				
				
//				int arrayDim = 0;
//				while(methodDesc.startsWith("[")) {
//					methodDesc = methodDesc.substring(1);
//					arrayDim++;
//				}
//				if(methodDesc.startsWith("L") && methodDesc.endsWith(";")) { // Ljava/lang/String;
//					methodDesc = methodDesc.substring(1, methodDesc.length() - 1).replace('/', '.');
//				} else if(methodDesc.length() != 1) {
//					throw new DecompileException("Method descriptor not recognised: " + methodDesc);
//				} else {
//					switch(methodDesc.charAt(0)) {
//					case 'B': methodDesc = "byte"; break;
//					case 'C': methodDesc = "char"; break;
//					case 'D': methodDesc = "double"; break;
//					case 'F': methodDesc = "float"; break;
//					case 'I': methodDesc = "int"; break;
//					case 'J': methodDesc = "long"; break;
//					case 'S': methodDesc = "short"; break;
//					case 'Z': methodDesc = "boolean"; break;
//					default: throw new DecompileException("Method description not recognised: " + methodDesc);
//					}
//				}
//				for(int dim = 0; dim < arrayDim; dim++) {
//					methodDesc += "[]";
//				}
				
				// Add the line.
				if(isConstructor) {
					decompStr += indentation + methodAccessFlagStr + " " + className + "(" + methodArgsStr + ")\n";
				} else {
					decompStr += indentation + methodAccessFlagStr + " " + returnType + " " + methodName + "(" + methodArgsStr + ")..." + methodDesc + "\n";
				}
				
			}
			decompStr += indentation + "\n";
		}
		
		
		
		// TODO - Decompile the rest.
		
		
		// Write the result to file.
		if(outputFile.isDirectory()) {
			throw new IOException("Cannot create file because a directory with the same name exists at: " + outputFile.getAbsolutePath());
		}
		FileOutputStream outStream = new FileOutputStream(outputFile);
		outStream.write(decompStr.getBytes(StandardCharsets.UTF_8));
		outStream.close();
	}
	
	@SuppressWarnings("serial")
	public static class DecompileException extends Exception {
		public DecompileException() {
			super();
		}
		public DecompileException(String message) {
			super(message);
		}
		public DecompileException(String message, Throwable cause) {
			super(message, cause);
		}
		public DecompileException(Throwable cause) {
			super(cause);
		}
	}
}
