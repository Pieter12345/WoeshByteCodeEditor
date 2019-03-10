package io.github.pieter12345.wbce.decompile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Stack;

import io.github.pieter12345.wbce.ByteCodeInstruction;
import io.github.pieter12345.wbce.ClassConstantPool;
import io.github.pieter12345.wbce.ClassFile;
import io.github.pieter12345.wbce.ClassFields.ClassField;
import io.github.pieter12345.wbce.ClassInterfaces.ClassInterface;
import io.github.pieter12345.wbce.ClassMethods.ClassMethod;
import io.github.pieter12345.wbce.constantpool.ConstantPoolClassRef;
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
		String packageStr = this.getPackage();
		if(packageStr != null) {
			decompStr += "package " + packageStr + ";\n\n";
		}
		
		// Create the "public abstract class ThisClass extends A implements B, C, D { ...".
		int classAccessFlags = this.classFile.getAccessFlags();
		String className = this.getClassName();
		
		String accessFlagStr = "";
		{
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
		}
		
		String extendsStr = "";
		{
			String superIdentifier = this.classFile.getSuperClassIdentifier().replace('/', '.');
			if(!superIdentifier.equals("java.lang.Object")) {
				extendsStr += " extends " + superIdentifier;
			}
		}
		
		String implementsStr = "";
		{
			ClassConstantPool constPool = this.classFile.getConstantPool();
			ClassInterface[] interfaces = this.classFile.getInterfaces().getInterfaces();
			for(int i = 0; i < interfaces.length; i++) {
				
				// Get the interface name (verifying it's in a ConstantPoolClassRef that points to a ConstantPoolString).
				if(!constPool.hasIndex(interfaces[i].getNameIndex())) {
					throw new DecompileException("Interface name index is not a valid index in the constant pool: " + interfaces[i].getNameIndex());
				}
				ConstantPoolObject nameObj = constPool.get(interfaces[i].getNameIndex());
				if(!(nameObj instanceof ConstantPoolClassRef)) {
					throw new DecompileException("Interface name index is expected to point at "
							+ ConstantPoolClassRef.class.getSimpleName() + " but found " + nameObj.getClass().getSimpleName() + ".");
				}
				int nameStrIndex = ((ConstantPoolClassRef) nameObj).getIndex();
				if(!constPool.hasIndex(nameStrIndex)) {
					throw new DecompileException("Interface name index (in a ConstantPoolClassRef) is not a valid index in the constant pool: "
							+ nameStrIndex + "(in ConstantPoolClassRef at index: " + interfaces[i].getNameIndex() + ")");
				}
				ConstantPoolObject nameStrObj = constPool.get(nameStrIndex);
				if(!(nameStrObj instanceof ConstantPoolString)) {
					throw new DecompileException("Interface name index in ConstantPoolClassRef is expected to point at "
							+ ConstantPoolString.class.getSimpleName() + " but found " + nameStrObj.getClass().getSimpleName() + ".");
				}
				String interfaceName = ((ConstantPoolString) nameStrObj).getString().replace('/', '.');
				
				// Add the interface to the list.
				implementsStr += (implementsStr.isEmpty() ? " implements " : ", ") + interfaceName;
			}
		}
		
		decompStr += indentation + accessFlagStr + className + extendsStr + implementsStr + " {\n";
		indentation += "\t";
		decompStr += indentation + "\n";
		
		// Create class fields. // TODO - Perhaps add simple assigns in the "static {this.i = 5;}" to the field declarations "private int i = 5;".
		String fieldsStr = this.getClassFields();
		if(fieldsStr != null) {
			decompStr += indentation + fieldsStr.replaceAll("\n", "\n" + indentation) + "\n";
		}
		
		// Create class methods.
		String methodssStr = this.getClassMethods();
		if(methodssStr != null) {
			decompStr += indentation + methodssStr.replaceAll("\n", "\n" + indentation) + "\n";
		}
		
		// Add the class-closing "}".
		decompStr += "}";
		
		// Write the result to file.
		if(outputFile.isDirectory()) {
			throw new IOException("Cannot create file because a directory with the same name exists at: " + outputFile.getAbsolutePath());
		}
		FileOutputStream outStream = new FileOutputStream(outputFile);
		outStream.write(decompStr.getBytes(StandardCharsets.UTF_8));
		outStream.close();
	}
	
	/**
	 * getPackage method.
	 * @return The package of the ClassFile in format: "my.package.name" or null if the class is in the default package.
	 * @throws DecompileException
	 */
	private String getPackage() throws DecompileException {
		String thisIdentifier = this.classFile.getThisClassIdentifier();
		if(thisIdentifier == null) {
			throw new DecompileException("This class identifier does not point to a valid location in the"
					+ " ClassConstantPool: " + this.classFile.getThisClassIdentifier());
		}
		if(thisIdentifier.contains("/")) {
			return thisIdentifier.replace('/', '.').substring(0, thisIdentifier.lastIndexOf('/'));
		}
		return null;
	}
	
	/**
	 * getClassName method.
	 * @return The class name of the ClassFile without its package path.
	 * @throws DecompileException
	 */
	private String getClassName() throws DecompileException {
		String thisIdentifier = this.classFile.getThisClassIdentifier();
		if(thisIdentifier == null) {
			throw new DecompileException("This class identifier does not point to a valid location in the"
					+ " ClassConstantPool: " + this.classFile.getThisClassIdentifier());
		}
		int index = thisIdentifier.indexOf('/');
		return (index == -1 ? thisIdentifier : thisIdentifier.substring(index + 1));
	}
	
	/**
	 * getClassFields method.
	 * @return All fields in format:<br>accessFlags fieldType fieldName;<br>accessFlags2 fieldType2 fieldName2;<br>etc. suffixed with a newline.<br>
	 * Returns null if the class does not contain any fields. This is valid java code when placed in the class scope.
	 * @throws DecompileException
	 */
	private String getClassFields() throws DecompileException {
		ClassConstantPool constPool = this.classFile.getConstantPool();
		ClassField[] fields = this.classFile.getFields().getFields();
		if(fields.length == 0) {
			return null;
		}
		String fieldsStr = "";
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
				if(fieldDesc.startsWith("java.lang.")) {
					fieldDesc = fieldDesc.substring("java.lang.".length());
				}
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
			fieldsStr += fieldAccessFlagStr + " " + fieldDesc + " " + fieldName + ";\n";
			
		}
		return fieldsStr;
	}
	
	/**
	 * getClassMethods method.
	 * @return All methods in valid java code or bytecode, depending on how much could be decompiled.
	 * Returns null if the class does not contain any methods.
	 * @throws DecompileException
	 */
	private String getClassMethods() throws DecompileException {
		// Create class methods.
		ClassConstantPool constPool = this.classFile.getConstantPool();
		ClassMethod[] methods = this.classFile.getMethods().getMethods();
		if(methods.length == 0) {
			return null;
		}
		String methodsStr = "";
		for(ClassMethod method : methods) {
			
			// Decompile the method.
			MethodDecompiler methodDecomp = new MethodDecompiler(this.getClassName(), method, constPool);
			String methodStr = methodDecomp.decompile();
			methodsStr += "\n" + methodStr;
			
//			// DEBUG
//			for(IAttribute attr : method.getAttributes().getAttributes()) {
//				System.out.println(attr.getClass().getSimpleName());
//			}
//			System.out.println();
			
			
//			// Get the access flags.
//			String methodAccessFlagStr = method.getAccessFlagString();
//			
//			// Get the method name.
//			if(!constPool.hasIndex(method.getNameIndex())) {
//				throw new DecompileException("Method name index is not a valid index in the constant pool: " + method.getNameIndex());
//			}
//			ConstantPoolObject nameObj = constPool.get(method.getNameIndex());
//			if(!(nameObj instanceof ConstantPoolString)) {
//				throw new DecompileException("Method name index is expected to point at "
//						+ ConstantPoolString.class.getSimpleName() + " but found " + nameObj.getClass().getSimpleName() + ".");
//			}
//			String methodName = ((ConstantPoolString) nameObj).getString();
//			
//			// Get the method description.
//			if(!constPool.hasIndex(method.getDescIndex())) {
//				throw new DecompileException("Method descriptor index is not a valid index in the constant pool: " + method.getDescIndex());
//			}
//			ConstantPoolObject descObj = constPool.get(method.getDescIndex());
//			if(!(descObj instanceof ConstantPoolString)) {
//				throw new DecompileException("Method descriptor index is expected to point at "
//						+ ConstantPoolString.class.getSimpleName() + " but found " + descObj.getClass().getSimpleName() + ".");
//			}
//			final String methodDesc = ((ConstantPoolString) descObj).getString();
//			
//			try {
//				// Convert the method description.
//				int splitIndex = methodDesc.indexOf(')');
//				if(!methodDesc.startsWith("(") || splitIndex == -1 || splitIndex == methodDesc.length() - 1) {
//					throw new DecompileException("Method descriptor not recognised: " + methodDesc);
//				}
//				
//				String returnType = methodDesc.substring(splitIndex + 1, methodDesc.length());
//				int arrayDim = 0;
//				while(returnType.startsWith("[")) {
//					returnType = returnType.substring(1);
//					arrayDim++;
//				}
//				if(returnType.startsWith("L") && returnType.endsWith(";")) { // Ljava/lang/String;
//					returnType = returnType.substring(1, returnType.length() - 1).replace('/', '.');
//					if(returnType.startsWith("java.lang.")) {
//						returnType = returnType.substring("java.lang.".length());
//					}
//				} else if(returnType.length() != 1) {
//					throw new DecompileException("Method descriptor return type not recognised: " + returnType);
//				} else {
//					switch(returnType.charAt(0)) {
//					case 'B': returnType = "byte"; break;
//					case 'C': returnType = "char"; break;
//					case 'D': returnType = "double"; break;
//					case 'F': returnType = "float"; break;
//					case 'I': returnType = "int"; break;
//					case 'J': returnType = "long"; break;
//					case 'S': returnType = "short"; break;
//					case 'Z': returnType = "boolean"; break;
//					case 'V': returnType = "void"; break;
//					default: throw new DecompileException("Method descriptor return type not recognised: " + returnType);
//					}
//				}
//				for(int dim = 0; dim < arrayDim; dim++) {
//					returnType += "[]";
//				}
//				
//				ArrayList<String> methodArgTypes = new ArrayList<String>();
//				int index = 1; // Start at 1 to skip the '('.
//				while(index < splitIndex) {
//					
//					arrayDim = 0;
//					while(methodDesc.startsWith("[", index)) {
//						index++;
//						arrayDim++;
//					}
//					
//					String methodArg = "";
//					switch(methodDesc.charAt(index)) {
//					case 'L':
//						int closingIndex = methodDesc.indexOf(';', index);
//						if(closingIndex == -1) {
//							throw new DecompileException("Method descriptor argument type not recognised in: " + methodDesc + " at index: " + index);
//						}
//						methodArg = methodDesc.substring(index + 1, closingIndex).replace('/', '.');
//						index += methodArg.length() + 1; // +1 for the ';'.
//						if(methodArg.startsWith("java.lang.")) {
//							methodArg = methodArg.substring("java.lang.".length());
//						}
//						break;
//					case 'B': methodArg = "byte"; break;
//					case 'C': methodArg = "char"; break;
//					case 'D': methodArg = "double"; break;
//					case 'F': methodArg = "float"; break;
//					case 'I': methodArg = "int"; break;
//					case 'J': methodArg = "long"; break;
//					case 'S': methodArg = "short"; break;
//					case 'Z': methodArg = "boolean"; break;
//					default: throw new DecompileException("Method descriptor argument type not recognised in: " + methodDesc + " at index: " + index);
//					}
//					index++;
//					
//					for(int dim = 0; dim < arrayDim; dim++) {
//						methodArg += "[]";
//					}
//					
//					methodArgTypes.add(methodArg);
//				}
//				
//				String methodArgsStr = "";
//				int argNum = 0;
//				for(String methodArgStr : methodArgTypes) {
//					// TODO - Get method argument variable names if available.
//					methodArgsStr += (methodArgsStr.isEmpty() ? "" : ", ") + methodArgStr + " arg" + argNum;
//					argNum++;
//				}
//				
//				// Get the method code.
//				IAttribute[] attributes = method.getAttributes().getAttributes();
//				CodeAttribute codeAttr = null;
//				for(IAttribute attr : attributes) {
//					if(attr instanceof CodeAttribute) {
//						if(codeAttr != null) {
//							throw new DecompileException("Method has more than 1 CodeAttribute: " + methodName + methodDesc);
//						}
//						codeAttr = (CodeAttribute) attr;
//					}
//				}
//				String methodCode;
//				if(codeAttr == null) {
//					// TODO - Throw an Exception if the method should have code (not abstract, not an interface).
//					methodCode = null;
//				} else {
//					methodCode = " " + this.getStringFromBytecode(codeAttr);
//					
////					System.out.println(methodName);
////					System.out.println(method.getAttributes().toString(constPool) + "\n");
//					
//				}
//				
//				// Add the line.
//				if(methodName.equals("<init>")) { // Constructor.
//					methodsStr += methodAccessFlagStr + " " + this.getClassName() + "(" + methodArgsStr + ")";
//				} else if(methodName.equals("<clinit>")) { // Static code block.
//					methodsStr += "static";
//				} else { // Normal method.
//					methodsStr += methodAccessFlagStr + " " + returnType + " " + methodName + "(" + methodArgsStr + ")";
//				}
//				if(methodCode != null) {
//					methodsStr += methodCode + "\n\n";
//				} else {
//					methodsStr += ";\n\n";
//				}
//				
//			} catch (DecompileException e) {
//				// Catch and rethrow this so the name of the failing method will be displayed.
//				throw new DecompileException("Failed to decompile method: " + methodName + methodDesc, e);
//			}
		}
		return methodsStr;
	}
	
	private void performStackModification(Stack<Object> stack, ByteCodeEntry instrEntry) throws DecompileException {
		ClassConstantPool constPool = this.classFile.getConstantPool();
		
		ByteCodeInstruction instr = instrEntry.getInstruction();
		int[] signedInstrArgs = instrEntry.getSignedInstructionArgs();
		int[] unsignedInstrArgs = instrEntry.getUnsignedInstructionArgs();
		
		switch(instr) {
		case nop: break;
		case aconst_null: stack.push(null); break;
		case iconst_m1: stack.push((int) -1); break;
		case iconst_0: stack.push((int) 0); break;
		case iconst_1: stack.push((int) 1); break;
		case iconst_2: stack.push((int) 2); break;
		case iconst_3: stack.push((int) 3); break;
		case iconst_4: stack.push((int) 4); break;
		case iconst_5: stack.push((int) 5); break;
		case lconst_0: stack.push((long) 0); break;
		case lconst_1: stack.push((long) 1); break;
		case fconst_0: stack.push((float) 0); break;
		case fconst_1: stack.push((float) 1); break;
		case fconst_2: stack.push((float) 2); break;
		case dconst_0: stack.push((double) 0); break;
		case dconst_1: stack.push((double) 1); break;
		case bipush: stack.push((int) signedInstrArgs[0]); break;
		case sipush: stack.push((int) signedInstrArgs[0]); break;
		case ldc: stack.push(constPool.get(unsignedInstrArgs[0])); break; // Push String, int or float.
		case ldc_w: stack.push(constPool.get(unsignedInstrArgs[0])); break; // Push String, int or float.
		case ldc2_w: stack.push(constPool.get(unsignedInstrArgs[0])); break; // Push double or long.
		case iload: stack.push(new LocalVariableRef(unsignedInstrArgs[0], LocalVariableRef.LocalVariableType.INT)); break;
		case lload: stack.push(new LocalVariableRef(unsignedInstrArgs[0], LocalVariableRef.LocalVariableType.LONG)); break;
		case fload: stack.push(new LocalVariableRef(unsignedInstrArgs[0], LocalVariableRef.LocalVariableType.FLOAT)); break;
		case dload: stack.push(new LocalVariableRef(unsignedInstrArgs[0], LocalVariableRef.LocalVariableType.DOUBLE)); break;
		case aload: stack.push(new LocalVariableRef(unsignedInstrArgs[0], LocalVariableRef.LocalVariableType.OBJECT_REF)); break;
		case iload_0: stack.push(new LocalVariableRef(0, LocalVariableRef.LocalVariableType.INT)); break;
		case iload_1: stack.push(new LocalVariableRef(1, LocalVariableRef.LocalVariableType.INT)); break;
		case iload_2: stack.push(new LocalVariableRef(2, LocalVariableRef.LocalVariableType.INT)); break;
		case iload_3: stack.push(new LocalVariableRef(3, LocalVariableRef.LocalVariableType.INT)); break;
		case lload_0: stack.push(new LocalVariableRef(0, LocalVariableRef.LocalVariableType.LONG)); break;
		case lload_1: stack.push(new LocalVariableRef(1, LocalVariableRef.LocalVariableType.LONG)); break;
		case lload_2: stack.push(new LocalVariableRef(2, LocalVariableRef.LocalVariableType.LONG)); break;
		case lload_3: stack.push(new LocalVariableRef(3, LocalVariableRef.LocalVariableType.LONG)); break;
		case fload_0: stack.push(new LocalVariableRef(0, LocalVariableRef.LocalVariableType.FLOAT)); break;
		case fload_1: stack.push(new LocalVariableRef(1, LocalVariableRef.LocalVariableType.FLOAT)); break;
		case fload_2: stack.push(new LocalVariableRef(2, LocalVariableRef.LocalVariableType.FLOAT)); break;
		case fload_3: stack.push(new LocalVariableRef(3, LocalVariableRef.LocalVariableType.FLOAT)); break;
		case dload_0: stack.push(new LocalVariableRef(0, LocalVariableRef.LocalVariableType.DOUBLE)); break;
		case dload_1: stack.push(new LocalVariableRef(1, LocalVariableRef.LocalVariableType.DOUBLE)); break;
		case dload_2: stack.push(new LocalVariableRef(2, LocalVariableRef.LocalVariableType.DOUBLE)); break;
		case dload_3: stack.push(new LocalVariableRef(3, LocalVariableRef.LocalVariableType.DOUBLE)); break;
		case aload_0: stack.push(new LocalVariableRef(0, LocalVariableRef.LocalVariableType.OBJECT_REF)); break;
		case aload_1: stack.push(new LocalVariableRef(1, LocalVariableRef.LocalVariableType.OBJECT_REF)); break;
		case aload_2: stack.push(new LocalVariableRef(2, LocalVariableRef.LocalVariableType.OBJECT_REF)); break;
		case aload_3: stack.push(new LocalVariableRef(3, LocalVariableRef.LocalVariableType.OBJECT_REF)); break;
		case iaload: break;
		case laload: break;
		case faload: break;
		case daload: break;
		case aaload: break;
		case baload: break;
		case caload: break;
		case saload: break;
		case istore: break;
		case lstore: break;
		case fstore: break;
		case dstore: break;
		case astore: break;
		case istore_0: break;
		case istore_1: break;
		case istore_2: break;
		case istore_3: break;
		case lstore_0: break;
		case lstore_1: break;
		case lstore_2: break;
		case lstore_3: break;
		case fstore_0: break;
		case fstore_1: break;
		case fstore_2: break;
		case fstore_3: break;
		case dstore_0: break;
		case dstore_1: break;
		case dstore_2: break;
		case dstore_3: break;
		case astore_0: break;
		case astore_1: break;
		case astore_2: break;
		case astore_3: break;
		case iastore: break;
		case lastore: break;
		case fastore: break;
		case dastore: break;
		case aastore: break;
		case bastore: break;
		case castore: break;
		case sastore: break;
		case pop: break;
		case pop2: break;
		case dup: break;
		case dup_x1: break;
		case dup_x2: break;
		case dup2: break;
		case dup2_x1: break;
		case dup2_x2: break;
		case swap: break;
		case iadd: break;
		case ladd: break;
		case fadd: break;
		case dadd: break;
		case isub: break;
		case lsub: break;
		case fsub: break;
		case dsub: break;
		case imul: break;
		case lmul: break;
		case fmul: break;
		case dmul: break;
		case idiv: break;
		case ldiv: break;
		case fdiv: break;
		case ddiv: break;
		case irem: break;
		case lrem: break;
		case frem: break;
		case drem: break;
		case ineg: break;
		case lneg: break;
		case fneg: break;
		case dneg: break;
		case ishl: break;
		case lshl: break;
		case ishr: break;
		case lshr: break;
		case iushr: break;
		case lushr: break;
		case iand: break;
		case land: break;
		case ior: break;
		case lor: break;
		case ixor: break;
		case lxor: break;
		case iinc: break;
		case i2l: break;
		case i2f: break;
		case i2d: break;
		case l2i: break;
		case l2f: break;
		case l2d: break;
		case f2i: break;
		case f2l: break;
		case f2d: break;
		case d2i: break;
		case d2l: break;
		case d2f: break;
		case i2b: break;
		case i2c: break;
		case i2s: break;
		case lcmp: break;
		case fcmpl: break;
		case fcmpg: break;
		case dcmpl: break;
		case dcmpg: break;
		case ifeq: break;
		case ifne: break;
		case iflt: break;
		case ifge: break;
		case ifgt: break;
		case ifle: break;
		case if_icmpeq: break;
		case if_icmpne: break;
		case if_icmplt: break;
		case if_icmpge: break;
		case if_icmpgt: break;
		case if_icmple: break;
		case if_acmpeq: break;
		case if_acmpne: break;
		case _goto: break;
		case jsr: break;
		case ret: break;
		case tableswitch: break;
		case lookupswitch: break;
		case ireturn: break;
		case lreturn: break;
		case freturn: break;
		case dreturn: break;
		case areturn: break;
		case _return: break;
		case getstatic: stack.push(constPool.get(unsignedInstrArgs[0])); break; // Push Field ref.
		case putstatic: break;
		case getfield: break;
		case putfield: break;
		case invokevirtual: break;
		case invokespecial: break;
		case invokestatic: break;
		case invokeinterface: break;
		case invokedynamic: break;
		case _new: break;
		case newarray: break;
		case anewarray: break;
		case arraylength: break;
		case athrow: break;
		case checkcast: break;
		case _instanceof: break;
		case monitorenter: break;
		case monitorexit: break;
		case wide: break;
		case multianewarray: break;
		case ifnull: break;
		case ifnonnull: break;
		case goto_w: break;
		case jsr_w: break;
		case breakpoint: break;
		case impdep1: break;
		case impdep2: break;
		default:
			throw new DecompileException("No stack handling code found for instruction: " + instr.getInstructionName());
		}
	}
	
	private static class LocalVariableRef {
		private final int index;
		private final LocalVariableType type;
		
		public LocalVariableRef(int index, LocalVariableType type) {
			this.index = index;
			this.type = type;
		}
		
		public int getIndex() {
			return this.index;
		}
		
		public LocalVariableType getType() {
			return this.type;
		}
		
		public enum LocalVariableType {
			BYTE,
			SHORT,
			INT,
			LONG,
			FLOAT,
			DOUBLE,
			CHAR,
			OBJECT_REF;
		}
	}
}
