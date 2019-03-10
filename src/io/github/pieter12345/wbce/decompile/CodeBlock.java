package io.github.pieter12345.wbce.decompile;

import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.Map;
import java.util.Stack;

import io.github.pieter12345.wbce.ByteCodeInstruction;
import io.github.pieter12345.wbce.ClassConstantPool;
import io.github.pieter12345.wbce.ClassMethods.ClassMethod;
import io.github.pieter12345.wbce.constantpool.ConstantPoolDouble;
import io.github.pieter12345.wbce.constantpool.ConstantPoolFloat;
import io.github.pieter12345.wbce.constantpool.ConstantPoolInteger;
import io.github.pieter12345.wbce.constantpool.ConstantPoolLong;
import io.github.pieter12345.wbce.constantpool.ConstantPoolObject;
import io.github.pieter12345.wbce.constantpool.ConstantPoolStringRef;
import io.github.pieter12345.wbce.decompile.CodeLabel.LabelType;

public class CodeBlock {
	
	// Variables & Constants.
	private String className;
	private ClassMethod method;
	private String[] methodArgTypes;
	private ClassConstantPool constPool;
	private ArrayList<ByteCodeEntry> byteCodeEntries;
	private Map<Integer, CodeLabel> labelMap;
	private CodeLabel label;
	private CodeLabel[] branchLabels;
	private ExceptionHandler[] exHandlers;
	private ByteCodeEntry[] codeEntries;
	private Stack<CodeStackObject> initialStack;
	private LocalVariableGen localVarMap;
	
	public CodeBlock(String className, ClassMethod parentMethod, String[] methodArgTypes, ClassConstantPool constPool,
			ArrayList<ByteCodeEntry> byteCodeEntries, Map<Integer, CodeLabel> labelMap, CodeLabel label,
			CodeLabel[] branchLabels, ExceptionHandler[] exHandlers, ByteCodeEntry[] codeEntries,
			Stack<CodeStackObject> initialStack, LocalVariableGen localVarMap) {
		if(codeEntries.length == 0) {
			throw new RuntimeException("codeEntries must not be empty.");
		}
		this.className = className;
		this.method = parentMethod;
		this.methodArgTypes = methodArgTypes;
		this.constPool = constPool;
		this.byteCodeEntries = byteCodeEntries;
		this.labelMap = labelMap;
		this.label = label;
		this.branchLabels = branchLabels;
		this.exHandlers = exHandlers;
		this.codeEntries = codeEntries;
		this.initialStack = initialStack;
		this.localVarMap = localVarMap;
	}
	
	public CodeLabel getLabel() {
		return this.label;
	}
	
	public ByteCodeEntry[] getCode() {
		return this.codeEntries;
	}
	
	public CodeLabel[] getBranchLabels() {
		return this.branchLabels;
	}
	
	public ExceptionHandler[] getExceptionHandlers() {
		return this.exHandlers;
	}
	
	@SuppressWarnings("unchecked")
	public String toDecompiledCodeString() {
		
		// Replace instructions with string representations.
		Stack<CodeStackObject> stack = (this.initialStack != null ? (Stack<CodeStackObject>) this.initialStack.clone() : new Stack<CodeStackObject>());

		String[] codeStrArray = new String[this.codeEntries.length];
		for(int i = 0; i < codeStrArray.length; i++) {
			codeStrArray[i] = null; // Initialize.
		}
		
		int objRefVarNum = 0; // Used for naming object references.
		
		try {
			for(int i = 0; i < this.codeEntries.length; i++) {
				Stack<CodeStackObject> stackClone = (Stack<CodeStackObject>) stack.clone();
				try {
					ByteCodeEntry instrEntry = this.codeEntries[i];
					ByteCodeInstruction instr = instrEntry.getInstruction();
					int[] signedInstrArgs = instrEntry.getSignedInstructionArgs();
					int[] unsignedInstrArgs = instrEntry.getUnsignedInstructionArgs();
					
//					// TODO - Remove debug.
//					// Print all bytecode instructions with their arguments.
//					String debugArgsStr = "";
//					for(int j = 0; j < signedInstrArgs.length; j++) {
//						debugArgsStr += signedInstrArgs[j] + "/" + unsignedInstrArgs[j] + " ";
//					}
//					System.out.println("[DEBUG] [CodeBlock] " + this.codeEntries[i].getInstructionIndex() + ": "
//							+ instr.getInstructionName() + " " + debugArgsStr.trim());
					
					switch(instr) {
					case nop: break;
					case aconst_null: stack.push(new CodeStackNull(i)); break;
					case iconst_m1: stack.push(new CodeStackConstValue(i, (int) -1)); break;
					case iconst_0: stack.push(new CodeStackConstValue(i, (int) 0)); break;
					case iconst_1: stack.push(new CodeStackConstValue(i, (int) 1)); break;
					case iconst_2: stack.push(new CodeStackConstValue(i, (int) 2)); break;
					case iconst_3: stack.push(new CodeStackConstValue(i, (int) 3)); break;
					case iconst_4: stack.push(new CodeStackConstValue(i, (int) 4)); break;
					case iconst_5: stack.push(new CodeStackConstValue(i, (int) 5)); break;
					case lconst_0: stack.push(new CodeStackConstValue(i, (long) 0)); break;
					case lconst_1: stack.push(new CodeStackConstValue(i, (long) 1)); break;
					case fconst_0: stack.push(new CodeStackConstValue(i, (float) 0)); break;
					case fconst_1: stack.push(new CodeStackConstValue(i, (float) 1)); break;
					case fconst_2: stack.push(new CodeStackConstValue(i, (float) 2)); break;
					case dconst_0: stack.push(new CodeStackConstValue(i, (double) 0)); break;
					case dconst_1: stack.push(new CodeStackConstValue(i, (double) 1)); break;
					case bipush: stack.push(new CodeStackConstValue(i, (int) signedInstrArgs[0])); break;
					case sipush: stack.push(new CodeStackConstValue(i, (int) signedInstrArgs[0])); break;
					case ldc:
					case ldc_w: { // Push String, float, int, Class, MethodType or MethodHandle.
						ConstantPoolObject constPoolObj = this.constPool.get(unsignedInstrArgs[0]);
						if(constPoolObj instanceof ConstantPoolStringRef) {
							stack.push(new CodeStackConstValue(i, constPoolObj.simpleVal(this.constPool))); // Push String.
						} else if(constPoolObj instanceof ConstantPoolFloat) {
							stack.push(new CodeStackConstValue(i, ((ConstantPoolFloat) constPoolObj).getValue())); // Push float.
						} else if(constPoolObj instanceof ConstantPoolInteger) {
							stack.push(new CodeStackConstValue(i, ((ConstantPoolInteger) constPoolObj).getValue())); // Push int.
						} else {
							// Docs state that ldc can also point to "Class", "MethodType" or "MethodHandle".
							throw new DecompileException(instr.getInstructionName() + " instruction pointed to an "
									+ "unexpected ConstantPoolObject: " + this.constPool.get(unsignedInstrArgs[0]).getClass().getSimpleName());
						}
						break;
					}
					case ldc2_w: { // Push long or double.
						ConstantPoolObject constPoolObj = this.constPool.get(unsignedInstrArgs[0]);
						if(constPoolObj instanceof ConstantPoolLong) {
							stack.push(new CodeStackConstValue(i, ((ConstantPoolLong) constPoolObj).getValue())); // Push long.
						} else if(constPoolObj instanceof ConstantPoolDouble) {
							stack.push(new CodeStackConstValue(i, ((ConstantPoolDouble) constPoolObj).getValue())); // Push double.
						} else {
							throw new DecompileException(instr.getInstructionName() + " instruction pointed to an "
									+ "unexpected ConstantPoolObject: " + this.constPool.get(unsignedInstrArgs[0]).getClass().getSimpleName());
						}
						break;
					}
					case iload: stack.push(new CodeStackVariable(i, this.localVarMap.getVar(unsignedInstrArgs[0]).getName(), "int")); break;
					case lload: stack.push(new CodeStackVariable(i, this.localVarMap.getVar(unsignedInstrArgs[0]).getName(), "long")); break;
					case fload: stack.push(new CodeStackVariable(i, this.localVarMap.getVar(unsignedInstrArgs[0]).getName(), "float")); break;
					case dload: stack.push(new CodeStackVariable(i, this.localVarMap.getVar(unsignedInstrArgs[0]).getName(), "double")); break;
					case aload: stack.push(new CodeStackVariable(i, this.localVarMap.getVar(unsignedInstrArgs[0]).getName(), this.localVarMap.getVar(unsignedInstrArgs[0]).getReturnType())); break;
					case iload_0: stack.push(new CodeStackVariable(i, this.localVarMap.getVar(0, "int").getName(), "int")); break;
					case iload_1: stack.push(new CodeStackVariable(i, this.localVarMap.getVar(1, "int").getName(), "int")); break;
					case iload_2: stack.push(new CodeStackVariable(i, this.localVarMap.getVar(2, "int").getName(), "int")); break;
					case iload_3: stack.push(new CodeStackVariable(i, this.localVarMap.getVar(3, "int").getName(), "int")); break;
					case lload_0: stack.push(new CodeStackVariable(i, this.localVarMap.getVar(0, "long").getName(), "long")); break;
					case lload_1: stack.push(new CodeStackVariable(i, this.localVarMap.getVar(1, "long").getName(), "long")); break;
					case lload_2: stack.push(new CodeStackVariable(i, this.localVarMap.getVar(2, "long").getName(), "long")); break;
					case lload_3: stack.push(new CodeStackVariable(i, this.localVarMap.getVar(3, "long").getName(), "long")); break;
					case fload_0: stack.push(new CodeStackVariable(i, this.localVarMap.getVar(0, "float").getName(), "float")); break;
					case fload_1: stack.push(new CodeStackVariable(i, this.localVarMap.getVar(1, "float").getName(), "float")); break;
					case fload_2: stack.push(new CodeStackVariable(i, this.localVarMap.getVar(2, "float").getName(), "float")); break;
					case fload_3: stack.push(new CodeStackVariable(i, this.localVarMap.getVar(3, "float").getName(), "float")); break;
					case dload_0: stack.push(new CodeStackVariable(i, this.localVarMap.getVar(0, "double").getName(), "double")); break;
					case dload_1: stack.push(new CodeStackVariable(i, this.localVarMap.getVar(1, "double").getName(), "double")); break;
					case dload_2: stack.push(new CodeStackVariable(i, this.localVarMap.getVar(2, "double").getName(), "double")); break;
					case dload_3: stack.push(new CodeStackVariable(i, this.localVarMap.getVar(3, "double").getName(), "double")); break;
					case aload_0: stack.push(new CodeStackVariable(i, this.localVarMap.getVar(0).getName(), this.localVarMap.getVar(0).getReturnType())); break;
					case aload_1: stack.push(new CodeStackVariable(i, this.localVarMap.getVar(1).getName(), this.localVarMap.getVar(1).getReturnType())); break;
					case aload_2: stack.push(new CodeStackVariable(i, this.localVarMap.getVar(2).getName(), this.localVarMap.getVar(2).getReturnType())); break;
					case aload_3: stack.push(new CodeStackVariable(i, this.localVarMap.getVar(3).getName(), this.localVarMap.getVar(3).getReturnType())); break;
					case iaload: {
						String arrayIndex = stack.pop().getDecompStr();
						String arrayRef = stack.pop().getDecompStr();
						if(arrayRef.indexOf(' ') != -1) {
							arrayRef = "(" + arrayRef + ")";
						}
						stack.push(new CodeStackCode(i, arrayRef + "[" + arrayIndex + "]", "int"));
						break;
					}
					case laload: {
						String arrayIndex = stack.pop().getDecompStr();
						String arrayRef = stack.pop().getDecompStr();
						if(arrayRef.indexOf(' ') != -1) {
							arrayRef = "(" + arrayRef + ")";
						}
						stack.push(new CodeStackCode(i, arrayRef + "[" + arrayIndex + "]", "long"));
						break;
					}
					case faload: {
						String arrayIndex = stack.pop().getDecompStr();
						String arrayRef = stack.pop().getDecompStr();
						if(arrayRef.indexOf(' ') != -1) {
							arrayRef = "(" + arrayRef + ")";
						}
						stack.push(new CodeStackCode(i, arrayRef + "[" + arrayIndex + "]", "float"));
						break;
					}
					case daload: {
						String arrayIndex = stack.pop().getDecompStr();
						String arrayRef = stack.pop().getDecompStr();
						if(arrayRef.indexOf(' ') != -1) {
							arrayRef = "(" + arrayRef + ")";
						}
						stack.push(new CodeStackCode(i, arrayRef + "[" + arrayIndex + "]", "double"));
						break;
					}
					case aaload: {
						String arrayIndex = stack.pop().getDecompStr();
						CodeStackObject arrayRefObj = stack.pop();
//						String arrayRef = arrayRefObj.getDecompStr().replace('/', '.');
						if(arrayRefObj.getReturnType() == null || !arrayRefObj.getReturnType().endsWith("[]")) {
							throw new DecompileException(instr.getInstructionName() + " expected an array, but found type: " + arrayRefObj.getReturnType()
									+ ". Array reference class: " + arrayRefObj.getClass().getSimpleName() + " { " + arrayRefObj.getDecompStr() + " }");
						}
						String returnType = arrayRefObj.getReturnType().substring(0, arrayRefObj.getReturnType().length() - 2); // Cut "[]" off.
						if(arrayRefObj instanceof CodeStackVariable) {
							CodeStackVariable arrayRefVar = (CodeStackVariable) arrayRefObj;
							stack.push(new CodeStackCode(i, arrayRefVar.getVarName() + "[" + arrayIndex + "]", returnType));
						} else if(arrayRefObj instanceof CodeStackCode) {
							String code = ((CodeStackCode) arrayRefObj).getDecompStr();
							if(code.indexOf(' ') != -1) {
								code = "(" + code + ")";
							}
							stack.push(new CodeStackCode(i, code + "[" + arrayIndex + "]", returnType));
						} else {
							throw new DecompileException(instr.getInstructionName() + " found an unexpected array reference: " + arrayRefObj.getClass().getSimpleName());
						}
//						System.out.println("DEBUG\t" + arrayIndex + "\t" + arrayRef + "\t" + arrayRefObj);
//						stack.push(new CodeStackCode(i, arrayRef + "[" + arrayIndex + "]", returnType));
						break;
					}
					case baload: {
						String arrayIndex = stack.pop().getDecompStr();
						String arrayRef = stack.pop().getDecompStr();
						if(arrayRef.indexOf(' ') != -1) {
							arrayRef = "(" + arrayRef + ")";
						}
						stack.push(new CodeStackCode(i, arrayRef + "[" + arrayIndex + "]", "byte"));
						break;
					}
					case caload: {
						String arrayIndex = stack.pop().getDecompStr();
						String arrayRef = stack.pop().getDecompStr();
						if(arrayRef.indexOf(' ') != -1) {
							arrayRef = "(" + arrayRef + ")";
						}
						stack.push(new CodeStackCode(i, arrayRef + "[" + arrayIndex + "]", "char"));
						break;
					}
					case saload: {
						String arrayIndex = stack.pop().getDecompStr();
						String arrayRef = stack.pop().getDecompStr();
						if(arrayRef.indexOf(' ') != -1) {
							arrayRef = "(" + arrayRef + ")";
						}
						stack.push(new CodeStackCode(i, arrayRef + "[" + arrayIndex + "]", "short"));
						break;
					}
					case istore:
					case lstore:
					case fstore:
					case dstore:
					case astore: {
						CodeStackObject codeObj = stack.pop();
						codeStrArray[i] = this.localVarMap.getVar(unsignedInstrArgs[0], codeObj.getReturnType()).getName() + " = " + codeObj.getDecompStr() + ";";
						break;
					}
					case istore_0: codeStrArray[i] = this.localVarMap.getVar(0, "int").getName() + " = " + stack.pop().getDecompStr() + ";"; break;
					case istore_1: codeStrArray[i] = this.localVarMap.getVar(1, "int").getName() + " = " + stack.pop().getDecompStr() + ";"; break;
					case istore_2: codeStrArray[i] = this.localVarMap.getVar(2, "int").getName() + " = " + stack.pop().getDecompStr() + ";"; break;
					case istore_3: codeStrArray[i] = this.localVarMap.getVar(3, "int").getName() + " = " + stack.pop().getDecompStr() + ";"; break;
					case lstore_0: codeStrArray[i] = this.localVarMap.getVar(0, "long").getName() + " = " + stack.pop().getDecompStr() + ";"; break;
					case lstore_1: codeStrArray[i] = this.localVarMap.getVar(1, "long").getName() + " = " + stack.pop().getDecompStr() + ";"; break;
					case lstore_2: codeStrArray[i] = this.localVarMap.getVar(2, "long").getName() + " = " + stack.pop().getDecompStr() + ";"; break;
					case lstore_3: codeStrArray[i] = this.localVarMap.getVar(3, "long").getName() + " = " + stack.pop().getDecompStr() + ";"; break;
					case fstore_0: codeStrArray[i] = this.localVarMap.getVar(0, "float").getName() + " = " + stack.pop().getDecompStr() + ";"; break;
					case fstore_1: codeStrArray[i] = this.localVarMap.getVar(1, "float").getName() + " = " + stack.pop().getDecompStr() + ";"; break;
					case fstore_2: codeStrArray[i] = this.localVarMap.getVar(2, "float").getName() + " = " + stack.pop().getDecompStr() + ";"; break;
					case fstore_3: codeStrArray[i] = this.localVarMap.getVar(3, "float").getName() + " = " + stack.pop().getDecompStr() + ";"; break;
					case dstore_0: codeStrArray[i] = this.localVarMap.getVar(0, "double").getName() + " = " + stack.pop().getDecompStr() + ";"; break;
					case dstore_1: codeStrArray[i] = this.localVarMap.getVar(1, "double").getName() + " = " + stack.pop().getDecompStr() + ";"; break;
					case dstore_2: codeStrArray[i] = this.localVarMap.getVar(2, "double").getName() + " = " + stack.pop().getDecompStr() + ";"; break;
					case dstore_3: codeStrArray[i] = this.localVarMap.getVar(3, "double").getName() + " = " + stack.pop().getDecompStr() + ";"; break;
					case astore_0: {
						CodeStackObject codeObj = stack.pop();
						codeStrArray[i] = this.localVarMap.getVar(0, codeObj.getReturnType()).getName() + " = " + codeObj.getDecompStr() + ";";
						break;
					}
					case astore_1: {
						CodeStackObject codeObj = stack.pop();
						codeStrArray[i] = this.localVarMap.getVar(1, codeObj.getReturnType()).getName() + " = " + codeObj.getDecompStr() + ";";
						break;
					}
					case astore_2: {
						CodeStackObject codeObj = stack.pop();
						codeStrArray[i] = this.localVarMap.getVar(2, codeObj.getReturnType()).getName() + " = " + codeObj.getDecompStr() + ";";
						break;
					}
					case astore_3: {
						CodeStackObject codeObj = stack.pop();
						codeStrArray[i] = this.localVarMap.getVar(3, codeObj.getReturnType()).getName() + " = " + codeObj.getDecompStr() + ";";
						break;
					}
					case iastore:
					case lastore:
					case fastore:
					case dastore:
					case aastore:
					case bastore:
					case castore:
					case sastore: {
						String value = stack.pop().getDecompStr();
						String arrayIndex = stack.pop().getDecompStr();
						String arrayRef = stack.pop().getDecompStr();
						if(arrayRef.indexOf(' ') != -1) {
							arrayRef = "(" + arrayRef + ")";
						}
						codeStrArray[i] = arrayRef + "[" + arrayIndex + "] = " + value + ";";
						break;
					}
					case pop: codeStrArray[i] = stack.pop().getDecompStr() + ";"; break;
					case pop2: throw new DecompileException("Found unsupported instruction: pop2 (Pops 1 or 2 items off the stack based on datatype)");
					case dup: {
						// Assign array references to variables if an array reference is duplicated.
						CodeStackObject obj = stack.peek();
						if(obj.getReturnType().endsWith("[]") && codeStrArray[obj.getInstructionIndex()] == null) {
							ByteCodeInstruction objInstr = this.codeEntries[obj.getInstructionIndex()].getInstruction();
							if(objInstr == ByteCodeInstruction.newarray
									|| objInstr == ByteCodeInstruction.anewarray || objInstr == ByteCodeInstruction.multianewarray) {
								String arrayRef = "obj" + objRefVarNum++;
								codeStrArray[obj.getInstructionIndex()] = obj.getReturnType() + " " + arrayRef + " = " + obj.getDecompStr() + ";";
								stack.pop(); // Remove the array declaration.
								stack.push(new CodeStackCode(obj.getInstructionIndex(), arrayRef, obj.getReturnType())); // Push the array reference.
							}
						}
						
						// Duplicate the top stack value.
						stack.push(stack.peek());
						break;
					}
					case dup_x1: {
						CodeStackObject topObj = stack.pop();
						CodeStackObject secondObj = stack.pop();
						stack.push(topObj);
						stack.push(secondObj);
						stack.push(topObj);
						break;
					}
					case dup_x2: {
						CodeStackObject topObj = stack.pop();
						CodeStackObject secondObj = stack.pop();
						CodeStackObject thirdObj = stack.pop();
						stack.push(topObj);
						stack.push(thirdObj);
						stack.push(secondObj);
						stack.push(topObj);
						break;
					}
					case dup2: throw new DecompileException("Found unsupported instruction: dup2_x1 (Duplicates 1 or 2 items on the stack based on datatype)");
					case dup2_x1: throw new DecompileException("Found unsupported instruction: dup2_x1 (Duplicates 1 or 2 items on the stack based on datatype)");
					case dup2_x2: throw new DecompileException("Found unsupported instruction: dup2_x1 (Duplicates 1 or 2 items on the stack based on datatype)");
					case swap: {
						CodeStackObject topObj = stack.pop();
						CodeStackObject secondObj = stack.pop();
						stack.push(topObj);
						stack.push(secondObj);
						break;
					}
					case iadd: this.handleMathOperator(i, stack, '+', "int"); break;
					case ladd: this.handleMathOperator(i, stack, '+', "long"); break;
					case fadd: this.handleMathOperator(i, stack, '+', "float"); break;
					case dadd: this.handleMathOperator(i, stack, '+', "double"); break;
					
					case isub: this.handleMathOperator(i, stack, '-', "int"); break;
					case lsub: this.handleMathOperator(i, stack, '-', "long"); break;
					case fsub: this.handleMathOperator(i, stack, '-', "float"); break;
					case dsub: this.handleMathOperator(i, stack, '-', "double"); break;

					case imul: this.handleMathOperator(i, stack, '*', "int"); break;
					case lmul: this.handleMathOperator(i, stack, '*', "long"); break;
					case fmul: this.handleMathOperator(i, stack, '*', "float"); break;
					case dmul: this.handleMathOperator(i, stack, '*', "double"); break;

					case idiv: this.handleMathOperator(i, stack, '/', "int"); break;
					case ldiv: this.handleMathOperator(i, stack, '/', "long"); break;
					case fdiv: this.handleMathOperator(i, stack, '/', "float"); break;
					case ddiv: this.handleMathOperator(i, stack, '/', "double"); break;

					case irem: this.handleMathOperator(i, stack, '%', "int"); break;
					case lrem: this.handleMathOperator(i, stack, '%', "long"); break;
					case frem: this.handleMathOperator(i, stack, '%', "float"); break;
					case drem: this.handleMathOperator(i, stack, '%', "double"); break;
					
					case ineg: stack.push(new CodeStackCode(i, "- " + stack.pop().getDecompStr(), "int")); break;
					case lneg: stack.push(new CodeStackCode(i, "- " + stack.pop().getDecompStr(), "long")); break;
					case fneg: stack.push(new CodeStackCode(i, "- " + stack.pop().getDecompStr(), "float")); break;
					case dneg: stack.push(new CodeStackCode(i, "- " + stack.pop().getDecompStr(), "double")); break;
					
					case ishl: {
						String shiftAmount = stack.pop().getDecompStr();
						String value = stack.pop().getDecompStr();
						stack.push(new CodeStackCode(i, "(" + value + ") << (" + shiftAmount + ")", "int"));
						break;
					}
					case lshl: {
						String shiftAmount = stack.pop().getDecompStr();
						String value = stack.pop().getDecompStr();
						stack.push(new CodeStackCode(i, "(" + value + ") << (" + shiftAmount + ")", "long"));
						break;
					}
					case ishr: {
						String shiftAmount = stack.pop().getDecompStr();
						String value = stack.pop().getDecompStr();
						stack.push(new CodeStackCode(i, "(" + value + ") >> (" + shiftAmount + ")", "int"));
						break;
					}
					case lshr: {
						String shiftAmount = stack.pop().getDecompStr();
						String value = stack.pop().getDecompStr();
						stack.push(new CodeStackCode(i, "(" + value + ") >> (" + shiftAmount + ")", "long"));
						break;
					}
					case iushr: {
						String shiftAmount = stack.pop().getDecompStr();
						String value = stack.pop().getDecompStr();
						stack.push(new CodeStackCode(i, "(" + value + ") >>> (" + shiftAmount + ")", "int"));
						break;
					}
					case lushr: {
						String shiftAmount = stack.pop().getDecompStr();
						String value = stack.pop().getDecompStr();
						stack.push(new CodeStackCode(i, "(" + value + ") >>> (" + shiftAmount + ")", "long"));
						break;
					}
					case iand: this.handleMathOperator(i, stack, '&', "int"); break;
					case land: this.handleMathOperator(i, stack, '&', "long"); break;
					case ior: this.handleMathOperator(i, stack, '|', "int"); break;
					case lor: this.handleMathOperator(i, stack, '|', "long"); break;
					case ixor: this.handleMathOperator(i, stack, '^', "int"); break;
					case lxor: this.handleMathOperator(i, stack, '^', "long"); break;
					case iinc: {
						String sign = (signedInstrArgs[1] >= 0 ? "+" : "-");
						int absVal = (signedInstrArgs[1] >= 0 ? signedInstrArgs[1] : -signedInstrArgs[1]);
						codeStrArray[i] = this.localVarMap.getVar(unsignedInstrArgs[0]).getName()
								+ (signedInstrArgs[1] == 1 || signedInstrArgs[1] == -1 ? sign + sign : " " + sign + "= " + absVal) + ";";
						break;
					}
					case i2l: stack.push(new CodeStackCode(i, "(long) (" + stack.pop().getDecompStr() + ")", "long")); break;
					case i2f: stack.push(new CodeStackCode(i, "(float) (" + stack.pop().getDecompStr() + ")", "float")); break;
					case i2d: stack.push(new CodeStackCode(i, "(double) (" + stack.pop().getDecompStr() + ")", "double")); break;
					case l2i: stack.push(new CodeStackCode(i, "(int) (" + stack.pop().getDecompStr() + ")", "int")); break;
					case l2f: stack.push(new CodeStackCode(i, "(float) (" + stack.pop().getDecompStr() + ")", "float")); break;
					case l2d: stack.push(new CodeStackCode(i, "(double) (" + stack.pop().getDecompStr() + ")", "double")); break;
					case f2i: stack.push(new CodeStackCode(i, "(int) (" + stack.pop().getDecompStr() + ")", "int")); break;
					case f2l: stack.push(new CodeStackCode(i, "(long) (" + stack.pop().getDecompStr() + ")", "long")); break;
					case f2d: stack.push(new CodeStackCode(i, "(double) (" + stack.pop().getDecompStr() + ")", "double")); break;
					case d2i: stack.push(new CodeStackCode(i, "(int) (" + stack.pop().getDecompStr() + ")", "int")); break;
					case d2l: stack.push(new CodeStackCode(i, "(long) (" + stack.pop().getDecompStr() + ")", "long")); break;
					case d2f: stack.push(new CodeStackCode(i, "(float) (" + stack.pop().getDecompStr() + ")", "float")); break;
					case i2b: stack.push(new CodeStackCode(i, "(byte) (" + stack.pop().getDecompStr() + ")", "byte")); break;
					case i2c: stack.push(new CodeStackCode(i, "(char) (" + stack.pop().getDecompStr() + ")", "char")); break;
					case i2s: stack.push(new CodeStackCode(i, "(short) (" + stack.pop().getDecompStr() + ")", "short")); break;
//					case lcmp: throw new DecompileException("Found unsupported instruction: " + instr.getInstructionName());
//					case fcmpl: throw new DecompileException("Found unsupported instruction: " + instr.getInstructionName());
//					case fcmpg: throw new DecompileException("Found unsupported instruction: " + instr.getInstructionName());
//					case dcmpl: throw new DecompileException("Found unsupported instruction: " + instr.getInstructionName());
//					case dcmpg: throw new DecompileException("Found unsupported instruction: " + instr.getInstructionName());
					case ifeq: codeStrArray[i] = this.handleIfXInstruction(i, stack, "==", true); break;
					case ifne: codeStrArray[i] = this.handleIfXInstruction(i, stack, "!=", true); break;
					case iflt: codeStrArray[i] = this.handleIfXInstruction(i, stack, "<" , true); break;
					case ifge: codeStrArray[i] = this.handleIfXInstruction(i, stack, ">=", true); break;
					case ifgt: codeStrArray[i] = this.handleIfXInstruction(i, stack, ">" , true); break;
					case ifle: codeStrArray[i] = this.handleIfXInstruction(i, stack, "<=", true); break;
					case if_icmpeq: codeStrArray[i] = this.handleIfXInstruction(i, stack, "==", false); break;
					case if_icmpne: codeStrArray[i] = this.handleIfXInstruction(i, stack, "!=", false); break;
					case if_icmplt: codeStrArray[i] = this.handleIfXInstruction(i, stack, "<" , false); break;
					case if_icmpge: codeStrArray[i] = this.handleIfXInstruction(i, stack, ">=", false); break;
					case if_icmpgt: codeStrArray[i] = this.handleIfXInstruction(i, stack, ">" , false); break;
					case if_icmple: codeStrArray[i] = this.handleIfXInstruction(i, stack, "<=", false); break;
					case if_acmpeq: codeStrArray[i] = this.handleIfXInstruction(i, stack, "==", false); break;
					case if_acmpne: codeStrArray[i] = this.handleIfXInstruction(i, stack, "!=", false); break;
					case _goto: codeStrArray[i] = "goto " + labelMap.get(this.getInstrIndexForBranch(byteCodeEntries, instrEntry)) + ";"; break;
//					case jsr: throw new DecompileException("Found unsupported instruction: " + instr.getInstructionName());
//					case ret: throw new DecompileException("Found unsupported instruction: " + instr.getInstructionName());
//					case tableswitch: throw new DecompileException("Found unsupported instruction: " + instr.getInstructionName());
					case lookupswitch: {
						// Just print the lookupswitch table for now. // TODO - Make this valid java code instead of just printing the table.
						// TODO - Make seperate code blocks for the code a lookupswitch can branch to (at code block generation).
						// TODO - Replace relative jumps with label jumps when labels exist for the targets.
						
						// lookupswitch: {<0-3 bytes padding> + (int) numberOfCases + (int) defaultRelJumpto
						// + BranchStackFrame[numberOfCases]} where BranchStackFrame: {(int) key, (int) relJumpto}.
						byte[] argsBytes = instrEntry.getRawInstructionArgBytes();
						int argsBytesIndex = 0;
						int index = instrEntry.getOffset();
						int paddingOffset = 3 - (index % 4); // Zero padding. Add zeros if index+1 (next int start) is not a multiple of 4.
						argsBytesIndex += paddingOffset;
						
						int defaultRelJumpto = ((argsBytes[argsBytesIndex] & 0xFF) << 24)
								| ((argsBytes[argsBytesIndex + 1] & 0xFF) << 16)
								| ((argsBytes[argsBytesIndex + 2] & 0xFF) << 8)
								| ((argsBytes[argsBytesIndex + 3] & 0xFF)); // Unsigned int.
						argsBytesIndex += 4;
						
						int numberOfCases = ((argsBytes[argsBytesIndex] & 0xFF) << 24)
								| ((argsBytes[argsBytesIndex + 1] & 0xFF) << 16)
								| ((argsBytes[argsBytesIndex + 2] & 0xFF) << 8)
								| ((argsBytes[argsBytesIndex + 3] & 0xFF)); // Unsigned int.
						argsBytesIndex += 4;
						
//						System.out.println("[DEBUG] lookupswitch padding=" + paddingOffset + " #cases=" + numberOfCases);
						
						String codeStr = "lookupswitch(" + stack.pop().getDecompStr() + "):\n\tdefault: +" + defaultRelJumpto;
						for(int j = 0; j < numberOfCases; j++) {
							int caseKey = ((argsBytes[argsBytesIndex] & 0xFF) << 24)
									| ((argsBytes[argsBytesIndex + 1] & 0xFF) << 16)
									| ((argsBytes[argsBytesIndex + 2] & 0xFF) << 8)
									| ((argsBytes[argsBytesIndex + 3] & 0xFF)); // Unsigned int.
							argsBytesIndex += 4;
							
							int caseRelJumpto = ((argsBytes[argsBytesIndex] & 0xFF) << 24)
									| ((argsBytes[argsBytesIndex + 1] & 0xFF) << 16)
									| ((argsBytes[argsBytesIndex + 2] & 0xFF) << 8)
									| ((argsBytes[argsBytesIndex + 3] & 0xFF)); // Unsigned int.
							argsBytesIndex += 4;
							
							codeStr += "\n\t" + caseKey + ": +" + caseRelJumpto;
						}
//						System.out.println("[DEBUG] Adding lookupswitch:\n" + codeStr);
						codeStrArray[i] = codeStr;
						break;
					}
					case ireturn:
					case lreturn:
					case freturn:
					case dreturn:
					case areturn: {
						String returnStr = stack.pop().getDecompStr();
						while(!stack.empty()) {
							CodeStackObject obj = stack.pop();
							int instrIndex = obj.getInstructionIndex();
							if(codeStrArray[instrIndex] == null) {
								codeStrArray[instrIndex] = obj.getDecompStr();
							} else {
								codeStrArray[instrIndex] += " /* Same line */ " + obj.getDecompStr();
							}
						}
						codeStrArray[i] = "return " + returnStr + ";";
						break;
					}
					case _return: {
						while(!stack.empty()) {
							CodeStackObject obj = stack.pop();
							int instrIndex = obj.getInstructionIndex();
							if(codeStrArray[instrIndex] == null) {
								codeStrArray[instrIndex] = obj.getDecompStr();
							} else {
								codeStrArray[instrIndex] += " /* Same line */ " + obj.getDecompStr();
							}
						}
						codeStrArray[i] = "return;";
						break;
					}
					case getstatic: { // Get static Field value.
						FieldRefDecoder fieldDecoder = new FieldRefDecoder(this.constPool, unsignedInstrArgs[0]);
						stack.push(new CodeStackCode(i, "(" + fieldDecoder.getFieldTypeDesc() + ") " + fieldDecoder.getClassName() + "." + fieldDecoder.getFieldName(), fieldDecoder.getFieldTypeDesc()));
						break;
					}
					case putstatic: { // Set static Field value.
						FieldRefDecoder fieldDecoder = new FieldRefDecoder(this.constPool, unsignedInstrArgs[0]);
						CodeStackObject valueObj = stack.pop();
						String valueStr = valueObj.getDecompStr();
						if(fieldDecoder.getFieldTypeDesc().equals("boolean") && valueObj.getReturnType() != null &&  valueObj.getReturnType().equals("int") && valueObj instanceof CodeStackConstValue) {
							int intVal = (int) ((CodeStackConstValue) valueObj).getValue();
							if(intVal != 0 && intVal != 1) {
								throw new DecompileException("Trying to assign an integer with value " + intVal + " to a boolean field.");
							}
							valueStr = (intVal == 1 ? "true" : "false");
						}
						codeStrArray[i] = fieldDecoder.getClassName()
								+ "." + fieldDecoder.getFieldName() + " = " + valueStr + ";";
						break;
					}
					case getfield: { // Get instance Field value.
						FieldRefDecoder fieldDecoder = new FieldRefDecoder(this.constPool, unsignedInstrArgs[0]);
						stack.push(new CodeStackCode(i, "(" + fieldDecoder.getFieldTypeDesc() + ") (" + stack.pop().getDecompStr() + ")." + fieldDecoder.getFieldName(), fieldDecoder.getFieldTypeDesc()));
						break;
					}
					case putfield: { // Set instance Field value.
						CodeStackObject valueObj = stack.pop();
						String objRefStr = stack.pop().getDecompStr();
						FieldRefDecoder fieldDecoder = new FieldRefDecoder(this.constPool, unsignedInstrArgs[0]);
						String valueStr = valueObj.getDecompStr();
						if(fieldDecoder.getFieldTypeDesc().equals("boolean") && valueObj.getReturnType() != null &&  valueObj.getReturnType().equals("int") && valueObj instanceof CodeStackConstValue) {
							int intVal = (int) ((CodeStackConstValue) valueObj).getValue();
							if(intVal != 0 && intVal != 1) {
								throw new DecompileException("Trying to assign an integer with value " + intVal + " to a boolean field.");
							}
							valueStr = (intVal == 1 ? "true" : "false");
						}
						if(objRefStr.indexOf(' ') != -1) {
							objRefStr = "(" + objRefStr + ")";
						}
						codeStrArray[i] = objRefStr + "." + fieldDecoder.getFieldName() + " = " + valueStr + ";";
						break;
					}
					case invokevirtual:
					case invokespecial: {
						MethodRefDecoder methodDecoder = new MethodRefDecoder(this.constPool, unsignedInstrArgs[0]);
						String methodArgs = "";
						String[] methodArgTypes = methodDecoder.getMethodArgsTypeDesc();
						for(int argNum = 0; argNum < methodArgTypes.length; argNum++) {
							String argType = methodArgTypes[methodArgTypes.length - 1 - argNum];
							CodeStackObject argObj = stack.pop();
							String argStr;
							if(argType.equals("boolean") && argObj.getReturnType() != null &&  argObj.getReturnType().equals("int") && argObj instanceof CodeStackConstValue) {
								int intVal = (int) ((CodeStackConstValue) argObj).getValue();
								if(intVal != 0 && intVal != 1) {
									throw new DecompileException("Trying to assign an integer with value " + intVal + " to a boolean field.");
								}
								argStr = (intVal == 1 ? "true" : "false");
							} else {
								String argObjStr = argObj.getDecompStr();
								if(argObjStr.indexOf(' ') != -1) {
									argObjStr = "(" + argObjStr + ")";
								}
								argStr = "(" + argType + ") " + argObjStr; // TODO - Don't add argType cast.
							}
							methodArgs = argStr + (methodArgs.isEmpty() ? "" : ", " + methodArgs);
						}
						CodeStackObject objRefObj = stack.pop();
						String objRefStr = objRefObj.getDecompStr();
						if(objRefStr.indexOf(' ') != -1) {
							objRefStr = "(" + objRefStr + ")";
						}
						
						if(methodDecoder.getMethodName().equals("<init>")) {
							// Finish a "new" instruction with the arguments passed to <init>. The code should be placed where the <init> is executed.
							if(codeStrArray[objRefObj.getInstructionIndex()] != null) {
								codeStrArray[i] = codeStrArray[objRefObj.getInstructionIndex()] + "(" + methodArgs + ");";
								codeStrArray[objRefObj.getInstructionIndex()] = null;
							} else if(this.byteCodeEntries.get(objRefObj.getInstructionIndex()).getInstruction() == ByteCodeInstruction.aload_0) {
								if(!methodArgs.equals("")) {
									codeStrArray[i] = "this(" + methodArgs + ");";
									codeStrArray[objRefObj.getInstructionIndex()] = null;
								}
							} else {
								throw new DecompileException("The <init> method was invoked, but there was no \"new Obj\" or aload_0 found.");
							}
							if(!methodDecoder.getMethodReturnTypeDesc().equals("void")) { // Should never happen.
								throw new DecompileException("<init> method did not return void at instruction index: " + i);
//								stack.push(new CodeStackCode(i, objRef, methodDecoder.getMethodReturnTypeDesc()));
							}
						} else if(methodDecoder.getMethodReturnTypeDesc().equals("void")) {
							codeStrArray[i] = objRefStr + "." + methodDecoder.getMethodName() + "(" + methodArgs + ");";
						} else {
							stack.push(new CodeStackCode(i, objRefStr + "." + methodDecoder.getMethodName() + "(" + methodArgs + ")", methodDecoder.getMethodReturnTypeDesc()));
						}
						break;
					}
					case invokestatic: {
						MethodRefDecoder methodDecoder = new MethodRefDecoder(this.constPool, unsignedInstrArgs[0]);
						String methodArgs = "";
						String[] methodArgTypes = methodDecoder.getMethodArgsTypeDesc();
						for(int argNum = 0; argNum < methodArgTypes.length; argNum++) {
							String argType = methodArgTypes[methodArgTypes.length - 1 - argNum];
							CodeStackObject argObj = stack.pop();
							String argStr;
							if(argType.equals("boolean") && argObj.getReturnType() != null &&  argObj.getReturnType().equals("int") && argObj instanceof CodeStackConstValue) {
								int intVal = (int) ((CodeStackConstValue) argObj).getValue();
								if(intVal != 0 && intVal != 1) {
									throw new DecompileException("Trying to assign an integer with value " + intVal + " to a boolean field.");
								}
								argStr = (intVal == 1 ? "true" : "false");
							} else {
								String argObjStr = argObj.getDecompStr();
								if(argObjStr.indexOf(' ') != -1) {
									argObjStr = "(" + argObjStr + ")";
								}
								argStr = "(" + argType + ") " + argObjStr; // TODO - Don't add argType cast.
							}
							methodArgs = argStr + (methodArgs.isEmpty() ? "" : ", " + methodArgs);
						}
						if(methodDecoder.getMethodReturnTypeDesc().equals("void")) {
							codeStrArray[i] = methodDecoder.getClassName() + "." + methodDecoder.getMethodName() + "(" + methodArgs + ");";
						} else {
							stack.push(new CodeStackCode(i, "" +  methodDecoder.getClassName() + "." + methodDecoder.getMethodName() + "(" + methodArgs + ")", methodDecoder.getMethodReturnTypeDesc()));
						}
						break;
					}
					case invokeinterface: {
						InterfaceRefDecoder interfaceDecoder = new InterfaceRefDecoder(this.constPool, unsignedInstrArgs[0]);
						String methodArgs = "";
						String[] methodArgTypes = interfaceDecoder.getMethodArgsTypeDesc();
						for(int argNum = 0; argNum < methodArgTypes.length; argNum++) {
							String argType = methodArgTypes[methodArgTypes.length - 1 - argNum];
							CodeStackObject argObj = stack.pop();
							String argStr;
							if(argType.equals("boolean") && argObj.getReturnType() != null &&  argObj.getReturnType().equals("int") && argObj instanceof CodeStackConstValue) {
								int intVal = (int) ((CodeStackConstValue) argObj).getValue();
								if(intVal != 0 && intVal != 1) {
									throw new DecompileException("Trying to assign an integer with value " + intVal + " to a boolean field.");
								}
								argStr = (intVal == 1 ? "true" : "false");
							} else {
								String argObjStr = argObj.getDecompStr();
								if(argObjStr.indexOf(' ') != -1) {
									argObjStr = "(" + argObjStr + ")";
								}
								argStr = "(" + argType + ") " + argObjStr; // TODO - Don't add argType cast.
							}
							methodArgs = argStr + (methodArgs.isEmpty() ? "" : ", " + methodArgs);
						}
						String objRefStr = stack.pop().getDecompStr();
						if(objRefStr.indexOf(' ') != -1) {
							objRefStr = "(" + objRefStr + ")";
						}
						if(interfaceDecoder.getMethodReturnTypeDesc().equals("void")) {
							codeStrArray[i] = objRefStr + "." + interfaceDecoder.getMethodName() + "(" + methodArgs + ");";
						} else {
							stack.push(new CodeStackCode(i, objRefStr + "." + interfaceDecoder.getMethodName() + "(" + methodArgs + ")", interfaceDecoder.getMethodReturnTypeDesc()));
						}
						break;
					}
					case invokedynamic: {
						MethodRefDecoder methodDecoder = new MethodRefDecoder(this.constPool, unsignedInstrArgs[0]);
						String methodArgs = "";
						String[] methodArgTypes = methodDecoder.getMethodArgsTypeDesc();
						for(int argNum = 0; argNum < methodArgTypes.length; argNum++) {
							String argType = methodArgTypes[methodArgTypes.length - 1 - argNum];
							CodeStackObject argObj = stack.pop();
							String argStr;
							if(argType.equals("boolean") && argObj.getReturnType() != null && argObj.getReturnType().equals("int") && argObj instanceof CodeStackConstValue) {
								int intVal = (int) ((CodeStackConstValue) argObj).getValue();
								if(intVal != 0 && intVal != 1) {
									throw new DecompileException("Trying to assign an integer with value " + intVal + " to a boolean field.");
								}
								argStr = (intVal == 1 ? "true" : "false");
							} else {
								String argObjStr = argObj.getDecompStr();
								if(argObjStr.indexOf(' ') != -1) {
									argObjStr = "(" + argObjStr + ")";
								}
								argStr = "(" + argType + ") " + argObjStr; // TODO - Don't add argType cast.
							}
							methodArgs = argStr + (methodArgs.isEmpty() ? "" : ", " + methodArgs);
						}
						if(methodDecoder.getMethodReturnTypeDesc().equals("void")) {
							codeStrArray[i] = methodDecoder.getClassName() + "." + methodDecoder.getMethodName() + "(" + methodArgs + ");";
						} else {
							stack.push(new CodeStackCode(i, methodDecoder.getClassName() + "." + methodDecoder.getMethodName() + "(" + methodArgs + ")", methodDecoder.getMethodReturnTypeDesc()));
						}
						break;
					}
					case _new: {
						ClassRefDecoder classDecoder = new ClassRefDecoder(this.constPool, unsignedInstrArgs[0]);
						String objRef = "obj" + objRefVarNum++;
						codeStrArray[i] = classDecoder.getClassName() + " " + objRef + " = new " + classDecoder.getClassName();
						stack.push(new CodeStackCode(i, objRef, classDecoder.getClassName()));
//						stack.push(new CodeStackCode(i, "new " + classDecoder.getClassName(), classDecoder.getClassName())); // This has to be completed with an invokespecial of the <init>(args...) method.
						break;
					}
					case newarray: {
						String type;
						switch(unsignedInstrArgs[0]) {
						case 4: type = "boolean"; break;
						case 5: type = "char"; break;
						case 6: type = "float"; break;
						case 7: type = "double"; break;
						case 8: type = "byte"; break;
						case 9: type = "short"; break;
						case 10: type = "int"; break;
						case 11: type = "long"; break;
						default:
							throw new DecompileException("Unknown primitive type index found in \"newarray\": " + unsignedInstrArgs[0]);
						}
						stack.push(new CodeStackCode(i, "new " + type + "[" + stack.pop().getDecompStr() + "]", type + "[]"));
						break;
					}
					case anewarray: {
						ClassRefDecoder classDecoder = new ClassRefDecoder(this.constPool, unsignedInstrArgs[0]);
						if(classDecoder.getArrayDim() != 0) {
							throw new DecompileException("Instruction anewarray was called with a ClassRef that already"
									+ " had an array dim specified (multianewarray should have been used): " + classDecoder.getClassName());
						}
						stack.push(new CodeStackCode(i, "new " + classDecoder.getClassName() + "[" + stack.pop().getDecompStr() + "]", classDecoder.getClassName() + "[]"));
						break;
					}
					case arraylength: {
						CodeStackObject arrayRef = stack.pop();
						stack.push(new CodeStackCode(i, "(" + arrayRef.getDecompStr() + ").length", "int"));
						break;
					}
					case athrow: {
						CodeStackObject exceptionRefObj = stack.pop();
						String exceptionRef = exceptionRefObj.getDecompStr();
						while(!stack.empty()) {
							CodeStackObject obj = stack.pop();
							int instrIndex = obj.getInstructionIndex();
							if(codeStrArray[instrIndex] == null) {
								codeStrArray[instrIndex] = obj.getDecompStr();
							} else {
								codeStrArray[instrIndex] += " /* Same line */ " + obj.getDecompStr();
							}
						}
						stack.push(exceptionRefObj);
						codeStrArray[i] = "throw " + exceptionRef + ";";
						break;
					}
					case checkcast: {
						ClassRefDecoder classDecoder = new ClassRefDecoder(this.constPool, unsignedInstrArgs[0]);
						String className = classDecoder.getClassName();
						String codeStr = stack.pop().getDecompStr();
						if(codeStr.indexOf(' ') != -1) {
							codeStr = "(" + codeStr + ")";
						}
						stack.push(new CodeStackCode(i, "(" + className + ") " + codeStr, className));
						break;
					}
					case _instanceof: {
						ClassRefDecoder classDecoder = new ClassRefDecoder(this.constPool, unsignedInstrArgs[0]);
						String className = classDecoder.getClassName();
						String codeStr = stack.pop().getDecompStr();
						if(codeStr.indexOf(' ') != -1) {
							codeStr = "(" + codeStr + ")";
						}
						stack.push(new CodeStackCode(i, codeStr + " instanceof " + className, "boolean"));
						break;
					}
	//				case monitorenter: break; // TODO - Implement instruction.
	//				case monitorexit: break;
	//				case wide: break;
					case multianewarray: {
						ClassRefDecoder classDecoder = new ClassRefDecoder(this.constPool, unsignedInstrArgs[0]);
						String codeStr = "";
						for(int j = 0; j < classDecoder.getArrayDim(); j++) {
							codeStr = "[" + stack.pop().getDecompStr() + "]" + codeStr;
						}
						codeStr = "new " + classDecoder.getClassName().replaceAll("\\[\\]", "") + codeStr;
						stack.push(new CodeStackCode(i, codeStr, classDecoder.getClassName()));
						break;
					}
					case ifnull: codeStrArray[i] = this.handleIfXInstruction(i, stack, "==", null); break;
					case ifnonnull: codeStrArray[i] = this.handleIfXInstruction(i, stack, "!=", null); break;
					case goto_w: codeStrArray[i] = "goto " + labelMap.get(this.getInstrIndexForBranch(byteCodeEntries, instrEntry)) + ";"; break;
					case jsr_w: throw new DecompileException("Found unsupported instruction: " + instr.getInstructionName());
					case breakpoint: throw new DecompileException("Found unsupported instruction: " + instr.getInstructionName());
					case impdep1: throw new DecompileException("Found unsupported instruction: " + instr.getInstructionName());
					case impdep2: throw new DecompileException("Found unsupported instruction: " + instr.getInstructionName());
					default:
						throw new DecompileException("No handling code found for instruction: " + instr.getInstructionName());
					}
					
	//				// TODO - Remove DEBUG.
	//				if(codeStrArray[i] != null) {
	//					System.out.println(codeStrArray[i]);
	//				}
				} catch (EmptyStackException e) {
					
					// Restore the stack to before the instruction that caused the Exception and add an extra stack element.
					stack = stackClone;
					stack.push(new CodeStackVariable(i, "MISSING_STACK_OBJECT", "Object")); // TODO - Perhaps make a new CodeStackBlaBlaBla for this?
					i--;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			
			// TODO - Remove after debugging.
			String methodArgsStr = "";
			for(String methodArgStr : methodArgTypes) {
				methodArgsStr += (methodArgsStr.isEmpty() ? "" : ", ") + methodArgStr;
			}
			System.out.println("[DEBUG] An exception was thrown, terminating the JVM. Class: " + this.className
					+ ". Method: " + this.constPool.get(this.method.getNameIndex()).simpleVal(this.constPool) + "(" + methodArgsStr + ")");
			System.exit(0);
		}
		
		// Append unused stack objects to see if code was skipped.
		while(!stack.isEmpty()) {
			CodeStackObject unhandledObject = stack.pop();
			if(stack.isEmpty() && this.codeEntries[this.codeEntries.length - 1].getInstruction() == ByteCodeInstruction.athrow) {
				break; // The remaining object is a thrown Throwable.
			}
			int instrIndex = unhandledObject.getInstructionIndex();
			if(codeStrArray[instrIndex] == null) {
				codeStrArray[instrIndex] = "// UNHANDLED STACK OBJECT: " + unhandledObject.getDecompStr().replaceAll("(\r|\n)", "~NEWLINE");
			} else {
				codeStrArray[instrIndex] += " // UNHANDLED STACK OBJECT: " + unhandledObject.getDecompStr().replaceAll("(\r|\n)", "~NEWLINE");
			}
		}
		
		// Generate the code string to return. This version only returns code, use the above code for label blocks.
		String ret = "";
		for(int i = 0; i < codeStrArray.length; i++) {
			
			// Add the code.
			if(codeStrArray[i] != null) {
				ret += "\n/*" + String.format("%3d", codeEntries[i].getInstructionIndex()) + " */ " + codeStrArray[i];
			}
			
		}
		return (ret.startsWith("\n") ? ret.substring(1) : ret);
		
	}
	
	private int getInstrIndexForBranch(ArrayList<ByteCodeEntry> byteCodeEntries, ByteCodeEntry branchCodeEntry) {
		int branchOffset = branchCodeEntry.getSignedInstructionArgs()[0];
		int absoluteIndex = branchCodeEntry.getOffset() + branchOffset;
		
		for(int i = 0; i < byteCodeEntries.size(); i++) {
			if(byteCodeEntries.get(i).getOffset() == absoluteIndex) {
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * handleMathOperator method.
	 * Takes 2 values off the stack, performs "secondVal OPERATOR firstVal" and pushes the result on the stack.
	 * @param instrIndex
	 * @param stack
	 * @param operator
	 * @param returnType
	 */
	private void handleMathOperator(int instrIndex, Stack<CodeStackObject> stack, char operator, String returnType) {
		String val2 = stack.pop().getDecompStr();
		if(val2.indexOf(' ') != -1) {
			val2 = "(" + val2 + ")";
		}
		String val1 = stack.pop().getDecompStr();
		if(val1.indexOf(' ') != -1) {
			val1 = "(" + val1 + ")";
		}
		stack.push(new CodeStackCode(instrIndex, val1 + " " + operator + " " + val2, returnType));
	}
	
	private String handleIfXInstruction(int instrIndex,
			Stack<CodeStackObject> stack, String operator, Boolean compareToZero) throws DecompileException {
		String val2 = (compareToZero == null ? "null"
				: (compareToZero.booleanValue() ? "0" : stack.pop().getDecompStr()));
		if(val2.indexOf(' ') != -1) {
			val2 = "(" + val2 + ")";
		}
		CodeStackObject obj1 = stack.pop();
		String val1 = obj1.getDecompStr();
		if(val1.indexOf(' ') != -1) {
			val1 = "(" + val1 + ")";
		}
		
		// Get the branch label.
		CodeLabel targetLabel = this.labelMap.get(
				this.getInstrIndexForBranch(this.byteCodeEntries, this.codeEntries[instrIndex]));
		String targetLabelStr = (targetLabel.getType() == LabelType.BRANCH_LABEL ? "L" : "CATCH_L") + targetLabel.getId();
		
		// Handle "if(someBoolean == 0)" and "if(someBoolean != 0)" and create the statement.
		String statement;
		if(obj1.getReturnType().equals("boolean") && compareToZero != null && compareToZero.booleanValue()) {
			if(operator.equals("==")) { // "if(obj1 == false)".
				statement = "!" + obj1.getDecompStr();
			} else if(operator.equals("!=")) { // "if(obj1 != false)".
				statement = obj1.getDecompStr();
			} else {
				throw new DecompileException("Found unexpected operator for boolean value: " + operator);
			}
		} else {
			statement = val1 + " " + operator + " " + val2;
		}

		return "if(" + statement + ") { goto " + targetLabelStr + "; }";
	}
}
