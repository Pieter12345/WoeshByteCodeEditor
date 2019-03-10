package io.github.pieter12345.wbce.decompile;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.pieter12345.wbce.ByteCodeInstruction;
import io.github.pieter12345.wbce.ClassConstantPool;
import io.github.pieter12345.wbce.ByteCodeInstruction.ByteCodeInstructionPayload;
import io.github.pieter12345.wbce.ClassMethods.ClassMethod;
import io.github.pieter12345.wbce.attribute.CodeAttribute;
import io.github.pieter12345.wbce.attribute.IAttribute;
import io.github.pieter12345.wbce.attribute.LocalVariableTableAttribute;
import io.github.pieter12345.wbce.attribute.CodeAttribute.ExceptionTable;
import io.github.pieter12345.wbce.attribute.CodeAttribute.ExceptionTableEntry;
import io.github.pieter12345.wbce.constantpool.ConstantPoolClassRef;
import io.github.pieter12345.wbce.constantpool.ConstantPoolObject;
import io.github.pieter12345.wbce.constantpool.ConstantPoolString;
import io.github.pieter12345.wbce.decompile.CodeBlockBounds.BoundsType;
import io.github.pieter12345.wbce.decompile.CodeLabel.LabelType;
import io.github.pieter12345.wbce.decompile.LocalVariableGen.LocalVariable;

public class MethodDecompiler {
	
	// Variables & constants.
	private final ClassMethod method;
	private final ClassConstantPool constPool;
	private final String className;
	private final boolean isStatic; // Used to determine if aload_0 loads "arg0" or "this".
	
	public MethodDecompiler(String className, ClassMethod method, ClassConstantPool constPool) {
		this.method = method;
		this.constPool = constPool;
		this.className = className;
		this.isStatic = (method.getAccessFlags() & 0x0008) == 0x0008;
	}
	
	public String decompile() throws DecompileException {
		
		// Get the access flags.
		String methodAccessFlagStr = this.method.getAccessFlagString();
		
		// Get the method name.
		if(!constPool.hasIndex(this.method.getNameIndex())) {
			throw new DecompileException(
					"Method name index is not a valid index in the constant pool: " + this.method.getNameIndex());
		}
		ConstantPoolObject nameObj = constPool.get(this.method.getNameIndex());
		if(!(nameObj instanceof ConstantPoolString)) {
			throw new DecompileException(
					"Method name index is expected to point at " + ConstantPoolString.class.getSimpleName()
					+ " but found " + nameObj.getClass().getSimpleName() + ".");
		}
		String methodName = ((ConstantPoolString) nameObj).getString();
		
		// Get the method description.
		if(!constPool.hasIndex(this.method.getDescIndex())) {
			throw new DecompileException(
					"Method descriptor index is not a valid index in the constant pool: " + this.method.getDescIndex());
		}
		ConstantPoolObject descObj = constPool.get(this.method.getDescIndex());
		if(!(descObj instanceof ConstantPoolString)) {
			throw new DecompileException(
					"Method descriptor index is expected to point at " + ConstantPoolString.class.getSimpleName()
					+ " but found " + descObj.getClass().getSimpleName() + ".");
		}
		final String methodDesc = ((ConstantPoolString) descObj).getString();
		
		try {
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
			
			// Get argument types from the method descriptor.
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
						throw new DecompileException("Method descriptor argument type not recognised in: "
								+ methodDesc + " at index: " + index);
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
				default: throw new DecompileException(
						"Method descriptor argument type not recognised in: " + methodDesc + " at index: " + index);
				}
				index++;
				
				for(int dim = 0; dim < arrayDim; dim++) {
					methodArg += "[]";
				}
				
				methodArgTypes.add(methodArg);
			}
			
			// Get the method CodeAttribute.
			IAttribute[] attributes = this.method.getAttributes().getAttributes();
			CodeAttribute codeAttr = null;
			for(IAttribute attr : attributes) {
				if(attr instanceof CodeAttribute) {
					if(codeAttr != null) {
						throw new DecompileException("Method has more than 1 CodeAttribute: "
								+ methodName + methodDesc);
					}
					codeAttr = (CodeAttribute) attr;
				}
			}
			if(codeAttr == null) {
				throw new DecompileException("CodeAttribute not found for method: " + methodName + methodDesc);
			}
			
			// Get argument names from the LocalVariableTableAttribute.
			String methodArgsStr = "";
			LocalVariableTableAttribute localVarTableAttr = null; 
			for(IAttribute attr : codeAttr.getAttributes().getAttributes()) {
				if(attr instanceof LocalVariableTableAttribute) {
					if(localVarTableAttr != null) {
						throw new DecompileException("Method has more than 1 LocalVariableTableAttribute: "
								+ methodName + methodDesc);
					}
					localVarTableAttr = (LocalVariableTableAttribute) attr;
				}
			}
			int localVarIndex = (this.isStatic ? 0 : 1); // If the method is not static, variable 0 will be "this".
			for(String methodArgStr : methodArgTypes) {
				// TODO - Check local variable type against the expected type.
				String argName = (localVarTableAttr != null
						? localVarTableAttr.getLocalVariable(localVarIndex++).getName(this.constPool)
						: "arg" + (localVarIndex++));
				methodArgsStr += (methodArgsStr.isEmpty() ? "" : ", ") + methodArgStr + " " + argName;
			}
			
			// Get the method code.
			String methodCode = " " + this.getStringFromBytecode(
					codeAttr, localVarTableAttr, methodArgTypes.toArray(new String[0]));
				
			// DEBUG - Print all attributes per method.
//			System.out.println(methodName);
//			System.out.println(method.getAttributes().toString(constPool) + "\n");
			
			// Format the method and return the result.
			String methodStr;
			if(methodName.equals("<init>")) { // Constructor.
				methodStr = methodAccessFlagStr + " " + this.className + "(" + methodArgsStr + ")";
			} else if(methodName.equals("<clinit>")) { // Static code block.
				methodStr = "static";
			} else { // Normal method.
				methodStr = methodAccessFlagStr + " " + returnType + " " + methodName + "(" + methodArgsStr + ")";
			}
			methodStr += methodCode;
			return methodStr.trim() + "\n";
			
		} catch (DecompileException e) {
			// Catch and rethrow this so the name of the failing method will be displayed.
			throw new DecompileException("Failed to decompile method: " + methodName + methodDesc, e);
		}
	}
	
	private String getStringFromBytecode(CodeAttribute codeAttr,
			LocalVariableTableAttribute localVarTableAttr, String[] methodArgTypes) throws DecompileException {
		byte[] opCodeBytes = codeAttr.getCodeBytes();
		
		// Convert the byte code to a list of instructions with their arguments and offset.
		ArrayList<ByteCodeEntry> byteCodeEntries = new ArrayList<ByteCodeEntry>();
		for(int index = 0; index < opCodeBytes.length; index++) {
			ByteCodeInstruction instr = ByteCodeInstruction.forOpCode(opCodeBytes[index]);
			if(instr == null) {
				throw new DecompileException("Unknown bytecode instruction ID found: " + opCodeBytes[index]);
			}
			int payload = instr.getPayload();
			if(instr == ByteCodeInstruction.lookupswitch) {
				
//				// TODO - Remove debug.
//				System.out.println("[DEBUG] Printing opcode bytes after lookupswitch:");
//				for(int i = index; i < opCodeBytes.length; i++) {
//					System.out.println(opCodeBytes[i] & 0xFF);
//				}
				
				// Calculate lookupswitch payload.
				// lookupswitch: {<0-3 bytes padding> + (int) numberOfCases + (int) defaultRelJumpto
				// + BranchStackFrame[numberOfCases]} where BranchStackFrame: {(int) key, (int) relJumpto}.
				int paddingOffset = 3 - (index % 4); // Zero padding. Add zeros if index+1 (next int start) is not a multiple of 4.
				int numberOfCases = ((opCodeBytes[index + paddingOffset + 5] & 0xFF) << 24)
						| ((opCodeBytes[index + paddingOffset + 6] & 0xFF) << 16)
						| ((opCodeBytes[index + paddingOffset + 7] & 0xFF) << 8)
						| ((opCodeBytes[index + paddingOffset + 8] & 0xFF)); // Unsigned int.
				payload = paddingOffset + (2 + numberOfCases * 2) * ByteCodeInstructionPayload.INT.getByteSize();
				
				// TODO - Remove debug.
//				System.out.println("[DEBUG] lookupswitch payload: " + payload + " for " + numberOfCases + " cases.");
			}
			if(payload == -1) {
				throw new DecompileException("ByteCodeInstruction \"" + instr + "\" has an unknown payload size.");
			}
			byte[] instrPayloadBytes = new byte[payload];
			System.arraycopy(opCodeBytes, index + 1, instrPayloadBytes, 0, instrPayloadBytes.length);
			
			byteCodeEntries.add(new ByteCodeEntry(index, byteCodeEntries.size(), instr, instrPayloadBytes));
			
			index += instrPayloadBytes.length;
		}
		
		// Initialize the code block bounds. This will contain the start/end/handle of Exceptions and
		// any place a branch instruction points at.
		Set<CodeBlockBounds> codeBlockBoundsSet = new TreeSet<CodeBlockBounds>(new Comparator<CodeBlockBounds>() {
			@Override
			public int compare(CodeBlockBounds val1, CodeBlockBounds val2) {
				return val1.getInstrIndex() - val2.getInstrIndex();
			}
		});
		codeBlockBoundsSet.add(new CodeBlockBounds(0, BoundsType.METHOD_START, -1));
		codeBlockBoundsSet.add(new CodeBlockBounds(byteCodeEntries.size(), BoundsType.METHOD_END, -1));
		
		// Get the ExceptionTable data.
		List<ExceptionHandler> exceptionHandlers =
				this.getExceptionHandlers(codeAttr.getExceptionTable(), byteCodeEntries);
		
		// Add the exception handler start/end/handler indices to the bounds list.
		for(ExceptionHandler exHandler : exceptionHandlers) {
			codeBlockBoundsSet.add(new CodeBlockBounds(exHandler.getStartPc(), BoundsType.START_PC, -1));
			codeBlockBoundsSet.add(new CodeBlockBounds(exHandler.getEndPc(), BoundsType.END_PC, -1));
			codeBlockBoundsSet.add(new CodeBlockBounds(exHandler.getHandlerPc(), BoundsType.HANDLER_PC, -1));
		}
		
		// Get the branch instruction target instruction indices.
		for(int i = 0; i < byteCodeEntries.size(); i++) {
			ByteCodeEntry codeEntry = byteCodeEntries.get(i);
			ByteCodeInstruction instr = codeEntry.getInstruction();
			int[] signedInstrArgs = codeEntry.getSignedInstructionArgs();
			if(instr.isBranchInstruction()) {
				if(signedInstrArgs.length != 1) {
					throw new DecompileException("Expecting branch instruction " + instr.getInstructionName()
							+ " to have argument size 1. Size was: " + codeEntry.getSignedInstructionArgs().length);
				}
				int branchOffset = signedInstrArgs[0];
				int absoluteIndex = codeEntry.getOffset() + branchOffset;
				
				codeBlockBoundsSet.add(new CodeBlockBounds(i + 1, BoundsType.BRANCH, i)); // Cut after the branch instruction.
				
				searchLoop: {
					for(int j = 0; j < byteCodeEntries.size(); j++) {
						if(byteCodeEntries.get(j).getOffset() == absoluteIndex) {
							codeBlockBoundsSet.add(new CodeBlockBounds(j, BoundsType.BRANCH, i));
							break searchLoop;
						}
					}
					throw new DecompileException("Branch instruction " + instr.getInstructionName()
							+ "(instruction index #" + i + ") pointed to an unexisting bytecode instruction index."
							+ " Relative: " + branchOffset + " / Absolute: " + absoluteIndex);
				}
			}
		}
		
		// Create a map with bytecode instruction indices and their corresponding labels.
		HashMap<Integer, CodeLabel> labelMap = new HashMap<Integer, CodeLabel>();
		int labelCount = 1;
		for(CodeBlockBounds bounds : codeBlockBoundsSet) {
			if(!labelMap.containsKey(bounds.getInstrIndex()) && bounds.getInstrIndex() < byteCodeEntries.size()) {
				LabelType type = (bounds.getType() == BoundsType.HANDLER_PC
						? LabelType.CATCH_LABEL : LabelType.BRANCH_LABEL);
				labelMap.put(bounds.getInstrIndex(), new CodeLabel(labelCount++, type));
			}
		}
		
		// Verify that there are no Xreturn or athrow instructions in the middle of code blocks (detect unreachable code).
		for(int i = 0; i < byteCodeEntries.size() - 1; i++) {
			ByteCodeEntry codeEntry = byteCodeEntries.get(i);
			ByteCodeInstruction instr = codeEntry.getInstruction();
			if((instr.isReturn() || instr == ByteCodeInstruction.athrow)
					&& !labelMap.containsKey(codeEntry.getInstructionIndex() + 1)) {
				throw new DecompileException("Found unreachable code at instruction index: " + codeEntry.getInstructionIndex());
			}
		}
		
		// Verify that there are no branches or bleeds to a catch block (handler_pc).
		for(ExceptionHandler exHandler : exceptionHandlers) {
			int handlerPc = exHandler.getHandlerPc();
			
			// Verify that code bleeds do not access catch blocks.
			ByteCodeInstruction prevBlockLastInstr = byteCodeEntries.get(handlerPc - 1).getInstruction();
			if(prevBlockLastInstr != ByteCodeInstruction._goto
					&& prevBlockLastInstr != ByteCodeInstruction.athrow && !prevBlockLastInstr.isReturn()) {
				throw new DecompileException("Detected code bleed to an exception handler (handler_pc)."
						+ " Last instruction of previous block was: " + prevBlockLastInstr.getInstructionName());
			}
			
			// Verify that code branches do not access catch blocks.
			Iterator<CodeBlockBounds> iterator = codeBlockBoundsSet.iterator();
			while(iterator.hasNext()) {
				CodeBlockBounds bounds = iterator.next();
				if(bounds.getInstrIndex() == handlerPc && bounds.getCause() != -1) {
					throw new DecompileException("Detected code branch to an exception handler (handler_pc)."
							+ " The branch instruction was: "
							+ byteCodeEntries.get(bounds.getInstrIndex()).getInstruction().getInstructionName()
							+ " At instruction index: " + bounds.getInstrIndex()
							+ " To catch block at instruction index: " + handlerPc);
				} else if(bounds.getInstrIndex() == handlerPc) {
// Code labels no longer have a name here. TODO - Remove this code if branch labels are implemented using CodeLabel.
//					// Rename catch code labels.
//					String handlerLabel = labelMap.get(handlerPc);
//					if(!handlerLabel.startsWith("CATCH_")) {
//						labelMap.put(handlerPc, "CATCH_" + handlerLabel);
//					}
				}
			}
		}
		
		// Create CodeBlock objects for each code block.
		ArrayList<CodeBlock> codeBlocks = new ArrayList<CodeBlock>(labelMap.size() - 1);
		CodeBlockBounds[] codeBlockBoundsArray = codeBlockBoundsSet.toArray(new CodeBlockBounds[0]);
		
		// TODO - Variable names are resolved here, but not checked against the expected type.
		// TODO - Mismatching local variable indices are not supported and not checked for.
		// TODO - Replace the local variable map by info from the LocalVariableTableAttribute.
		LocalVariableGen localVarMap = new LocalVariableGen();
		int localVarIndex = 0;
		boolean isMethodStatic = (this.method.getAccessFlags() & 0x0008) == 0x0008;
		if(!isMethodStatic) {
			if(localVarTableAttr != null) {
				LocalVariableTableAttribute.LocalVariable localVar = localVarTableAttr.getLocalVariable(localVarIndex);
				localVarMap.setVar(localVarIndex, new LocalVariable(localVar.getName(this.constPool), this.className));
			} else {
				localVarMap.setVar(localVarIndex, new LocalVariable("this", this.className));
			}
			localVarIndex++;
		}
		for(int i = 0; i < methodArgTypes.length; i++) {
			if(localVarTableAttr != null) {
				LocalVariableTableAttribute.LocalVariable localVar = localVarTableAttr.getLocalVariable(localVarIndex);
				localVarMap.setVar(localVarIndex,
						new LocalVariable(localVar.getName(this.constPool), methodArgTypes[i]));
			} else {
				localVarMap.setVar(localVarIndex, new LocalVariable("arg" + localVarIndex, methodArgTypes[i]));
			}
			localVarIndex++;
		}
		
		for(int i = 0; i < codeBlockBoundsArray.length - 1; i++) {
			if(codeBlockBoundsArray[i].getInstrIndex() == codeBlockBoundsArray[i + 1].getInstrIndex()) {
				continue; // Skip double bounds on the same index.
			}
			
			// Get a list of code entries.
			ByteCodeEntry[] entries = byteCodeEntries.subList(
					codeBlockBoundsArray[i].getInstrIndex(), codeBlockBoundsArray[i + 1].getInstrIndex())
					.toArray(new ByteCodeEntry[0]);
			
			// Get the ExceptionHandlers containing this code (Some might never be a handler since earlier handlers catch them already).
			ArrayList<ExceptionHandler> exHandlerList = new ArrayList<ExceptionHandler>();
			for(ExceptionHandler exHandler : exceptionHandlers) {
				if(codeBlockBoundsArray[i].getInstrIndex() >= exHandler.getStartPc() && codeBlockBoundsArray[i].getInstrIndex() < exHandler.getEndPc()) {
					exHandlerList.add(exHandler);
				}
			}
			ExceptionHandler[] exHandlers = exHandlerList.toArray(new ExceptionHandler[0]);
			
			// Get the labels this code block can branch to (not throw).
			ArrayList<CodeLabel> branchLabels = new ArrayList<CodeLabel>();
			ByteCodeEntry lastCodeEntry = entries[entries.length - 1];
			if(lastCodeEntry.getInstruction().isBranchInstruction()) {
				int branchTargetIndex = this.getInstrIndexForBranch(byteCodeEntries, lastCodeEntry);
				branchLabels.add(labelMap.get(branchTargetIndex)); // Branch.
			}
			if(!lastCodeEntry.getInstruction().isReturn() && lastCodeEntry.getInstruction() != ByteCodeInstruction.athrow
					&& lastCodeEntry.getInstruction() != ByteCodeInstruction._goto) {
				if(lastCodeEntry.getInstructionIndex() >= byteCodeEntries.size() - 1) {
					throw new DecompileException("Last bytecode instruction does not branch, return or throw.");
				}
				int branchTargetIndex = lastCodeEntry.getInstructionIndex() + 1;
				branchLabels.add(labelMap.get(branchTargetIndex)); // Bleed.
			}
			CodeLabel[] canBranchToLabels = branchLabels.toArray(new CodeLabel[0]);
			
			// Generate the initial stack.
			Stack<CodeStackObject> initialStack = new Stack<CodeStackObject>();
			for(ExceptionHandler exHandler : exceptionHandlers) {
				if(codeBlockBoundsArray[i].getInstrIndex() == exHandler.getHandlerPc()) {
					initialStack.push(new CodeStackCode(0, "exception", exHandler.getExceptionType()));
					break;
				}
			}
			
			// Create and add the code block.
			codeBlocks.add(new CodeBlock(this.className, this.method, methodArgTypes, this.constPool, byteCodeEntries, labelMap,
					labelMap.get(codeBlockBoundsArray[i].getInstrIndex()), canBranchToLabels, exHandlers, entries, initialStack, localVarMap));
			
		}
		
		/* TODO - Generate a code path graph from the code blocks (or other data if necessary).
		 * Nodes are code blocks (with no branching targets in them except for the first instruction).
		 * Edges are branches.
		 * Code blocks can branch to 1 or 2 code blocks and can be branched to by 0 (initial node) to many nodes.
		 * Use this graph to detect if/for/while/switch/try/catch structures and store them somehow.
		 * Then, modify the CodeBlock code to be able to handle stack elements properly.
		 * 
		 * Code structures:
		 * 
		 * If structure:
		 *     1
		 *     if(cond) {
		 *         2
		 *     }
		 *     3
		 *     
		 *     -> 1 -> 2 -> 3 ->
		 *        |         ^
		 *        |         |
		 *         ---------
		 *     
		 *     
		 * If else structure:
		 *     1
		 *     if(cond) {
		 *         2
		 *     } else {
		 *         3
		 *     }
		 *     4
		 *     
		 *     -> 1 -> 2 -> 4 ->
		 *        |         ^
		 *        |         |
		 *         --> 3 ---
		 *     
		 * While structure:
		 *     1
		 *     while(c) {
		 *         2
		 *     }
		 *     3
		 *     
		 *             3
		 *             ^
		 *             |
		 *     -> 1 -> c -> 2
		 *             ^    |
		 *             |    |
		 *              ----
		 *     
		 *     1
		 *     while(true) {
		 *         2 (if cond, then break)
		 *     }
		 *     3
		 *     
		 *          3
		 *          ^
		 *          |
		 *     1 -> 2 ---  
		 *          ^    |
		 *          |    |
		 *           ----
		 * 
		 * For structure:
		 *     1
		 *     for(a; c; i) {
		 *         2
		 *     }
		 *     3
		 *     
		 *                  3
		 *                  ^
		 *                  |
		 *     -> 1 -> a -> c -> 2
		 *                  ^    |
		 *                  |    v
		 *                   --- i
		 *     
		 *     1
		 *     for(;;) {
		 *         2 (if cond, then break)
		 *     }
		 *     3
		 *     
		 *             3
		 *             ^
		 *             |
		 *     -> 1 -> 2 ---
		 *             ^    |
		 *             |    |
		 *              ----
		 * 
		 * Strategy:
		 * Nodes can contain multiple code blocks.
		 * 
		 * Handle loops:
		 * 1. Detect loops:
		 *     Iterate over the graph depth-first from the root and detect circular paths.
		 *     Store the first overlapping node of each loop (2 in 1 -> 2 -> 3 -> 2 -> ...).
		 *     Store the other path this overlapping node has as a valid 'break' target for code within the loop.
		 *         For later: Don't forget to add labels where necessary to break from the right loop.
		 * 2. For code in each loop, detect if branches are leading to accepted 'break' targets for the loop
		 *    or a parent and merge nodes with these breaks with their other child (if they have one).
		 * 3. If a loop does not contain a loop, resolve the if/else/switch/case/try/catch structure.
		 * 
		 * Handle non-loop code (after handling loop code, so loops and loop breaks are handled here):
		 * 1. Detect ifs:
		 *     These are node groups in the graph which have exactly one input and one output.
		 * 
		 * 
		 * Note: Loops always (?) start with a GOTO instruction that branches to the condition of the loop at the very
		 * end of the loop. This condition can then branch back to the instruction after this first GOTO instruction.
		 * The increment code used in for loops is just above the condition code and is only executed after the actual
		 * code in the loop executes.
		 * If a loop does not start with a GOTO instruction that skips some code to execute some condition first, this
		 * means that there is no loop condition (while(true) or for(;;)).
		 * 
		 * 
		 * TODO - When this is implemented, return bytecode within if/else/for/while etc statements.
		 */
		
		// Loop detection: Find all code blocks that are being referenced to from further code (loops).
		// TODO - Remove if not needed. Other lower level implementation below.
//		List<CodeBlock> loopBlocks = new ArrayList<CodeBlock>();
//		for(CodeBlock block : codeBlocks) {
//			ByteCodeEntry[] entries = block.getCode();
//			ByteCodeEntry lastCodeEntry = entries[entries.length - 1];
//			if(lastCodeEntry.getInstruction().isBranchInstruction()) {
//				int branchTargetIndex = this.getInstrIndexForBranch(byteCodeEntries, lastCodeEntry);
//				if(branchTargetIndex <= lastCodeEntry.getInstructionIndex()) {
//					
//					// This branch is to the past, so find the target (generally loop condition code) and store that.
//					for(CodeBlock block2 : codeBlocks) {
//						if(block2.getCode()[0].getInstructionIndex() == branchTargetIndex) {
//							loopBlocks.add(block2);
//							break;
//						}
//					}
//				}
//			}
//		}
		
		// Detect loops.
		List<CodeLoop> codeLoops = new ArrayList<CodeLoop>();
		Set<Integer> loopStartInstrIndices = new HashSet<Integer>(); // Set containing the first instr of all loops.
		for(int i = byteCodeEntries.size() - 1; i >= 0; i--) {
			ByteCodeEntry codeEntry = byteCodeEntries.get(i);
			
			// Check if the instruction is a branch instruction to a target in earlier bytecode.
			if(codeEntry.getInstruction().isBranchInstruction() && codeEntry.getSignedInstructionArgs()[0] <= 0) {
				
				// Get the branch target instruction index. This is the start of the loop.
				int loopStartIndex = this.getInstrIndexForBranch(byteCodeEntries, codeEntry);
				assert loopStartIndex != -1;
				int loopEndIndex = codeEntry.getInstructionIndex();
				
				// Get the loop break target. This is the instruction index a "break" would branch to.
				int loopBreakTarget;
				if(codeEntry.getInstruction() == ByteCodeInstruction._goto
						|| codeEntry.getInstruction() == ByteCodeInstruction.goto_w) {
					
					// If a break target exists, it is the lowest index after the loop that can be branched to from
					// within the loop.
					loopBreakTarget = Integer.MAX_VALUE;
					for(int j = loopStartIndex; j < loopEndIndex; j++) {
						ByteCodeEntry loopCodeEntry = byteCodeEntries.get(j);
						if(loopCodeEntry.getInstruction().isBranchInstruction()) {
							int branchTarget = this.getInstrIndexForBranch(byteCodeEntries, loopCodeEntry);
							assert branchTarget != -1; // Branch target must exist.
							if(branchTarget > loopEndIndex && branchTarget < loopBreakTarget) {
								loopBreakTarget = branchTarget;
							}
						}
					}
					if(loopBreakTarget == Integer.MAX_VALUE) {
						throw new DecompileException("Unable to find \"break\" index for loop."
								+ " Loop start index: " + loopStartIndex + ", loop end index: " + loopEndIndex);
					}
					
				} else {
					
					// When the loop condition breaks, the next line is branched to.
					assert loopEndIndex + 1 < byteCodeEntries.size(); // A branch leads to this line, so it must exist.
					loopBreakTarget = loopEndIndex + 1;
				}
				
				// Check if the instruction above the loop can access the loop directly. This is the case for
				// unconditional loops (while(true), for(;;)) and "do {...} while(...)" structures.
				// Since unconditional loops are equal to "do {...} while(true)", this structure can always be used
				// in this case.
				boolean isDoWhile = loopStartIndex == 0
						|| (byteCodeEntries.get(loopStartIndex - 1).getInstruction() != ByteCodeInstruction._goto
						&& byteCodeEntries.get(loopStartIndex - 1).getInstruction() != ByteCodeInstruction.goto_w);
				
				// Get the loop condition start index.
				int conditionStartIndex = -1;
				if(!isDoWhile) {
					
					// The loop starts with a GOTO that directly points at the condition.
					conditionStartIndex = this.getInstrIndexForBranch(
							byteCodeEntries, byteCodeEntries.get(loopStartIndex - 1));
					assert conditionStartIndex != -1;
				} else {
					
					// The condition start is the lowest index from the condition end, without passing a code bounds.
					// When conditions are conditional ("while(cond1 ? cond2 : cond3) {...}"), find the lowest index
					// from the last branch that directly continues the loop, without passing a code bounds.
					for(int j = codeBlockBoundsArray.length - 1; j >= 0; j--) {
						CodeBlockBounds codeBlockBounds = codeBlockBoundsArray[j];
						int boundsInstrIndex = codeBlockBounds.getInstrIndex();
						
						// Check loop bounds.
						if(boundsInstrIndex >= loopStartIndex && boundsInstrIndex < loopEndIndex) {
							if(conditionStartIndex == -1) {
								conditionStartIndex = boundsInstrIndex;
							} else {
								
								// Overwrite the start index if branches directly branch to the loop start.
								// When this happens, it means that the loop condition itself is conditional.
								// Example: "while(cond1 ? cond2 : cond3) {...}".
								
								// Get the last instruction of the current code block.
								CodeBlockBounds nextCodeBlockBounds = codeBlockBoundsArray[j + 1];
								ByteCodeEntry lastCodeEntry =
										byteCodeEntries.get(nextCodeBlockBounds.getInstrIndex() - 1);
								
								// Overwrite the start index if this last instruction branches to the loop start.
								if(lastCodeEntry.getInstruction().isBranchInstruction()
										&& this.getInstrIndexForBranch(
										byteCodeEntries, lastCodeEntry) == loopStartIndex) {
									conditionStartIndex = boundsInstrIndex;
								}
							}
						}
					}
					assert conditionStartIndex != -1;
				}
				
				// Skip the condition indices to prevent them being detected as loop branches.
				i = conditionStartIndex;
				
				// Verify that the loop start index has not been used for another loop.
				if(loopStartInstrIndices.contains(loopStartIndex)) {
					throw new DecompileException("Loop start is branched to directly more than once."
							+ " Loop start index: " + loopStartIndex + ", loop end index: " + loopEndIndex
							+ ", loop condition start index: " + conditionStartIndex);
				}
				
				// Store the loop.
				codeLoops.add(new CodeLoop(loopStartIndex, conditionStartIndex, loopEndIndex, loopBreakTarget));
				loopStartInstrIndices.add(loopStartIndex);
//				System.out.println("[DEBUG] [MethodDecompiler] Found loop: Start " + loopStartIndex
//						+ " - Cond start " + conditionStartIndex + " - End " + loopEndIndex);
			}
		}
		
		// Create a map of all "continue" and "break" branches and the loops they continue to / break from.
		Map<Integer, CodeLoop> loopContinueInstrIndicesMap = new HashMap<Integer, CodeLoop>();
		Map<Integer, CodeLoop> loopBreakInstrIndicesMap = new HashMap<Integer, CodeLoop>();
		for(CodeLoop codeLoop : codeLoops) {
			for(int i = codeLoop.getStartInstrIndex(); i < codeLoop.getConditionStartInstrIndex(); i++) {
				ByteCodeEntry codeEntry = byteCodeEntries.get(i);
				if(codeEntry.getInstruction().isBranchInstruction()) {
					int branchTarget = this.getInstrIndexForBranch(byteCodeEntries, codeEntry);
					/* TODO - This fails due to branches directly back to the loop start being seen as part of the
					 * condition. This can probably be resolved by detecting no condition and making a while(true)
					 * loop so that the branches directly to the method start can be "continue" and the loop condition
					 * can be detected as "break" target.
					 * Another possibility is that this code is part of some conditional condition. If that is the
					 * case, then this failure means that the condition code should contain this (and probably more)
					 * code block(s).
					 */
					assert branchTarget <= codeLoop.getConditionStartInstrIndex()
							|| branchTarget > codeLoop.getEndInstrIndex()
							: "Loop code branch accesses the middle of the condition code. Method: "
							+ this.method.getName(this.constPool) + this.method.getDesc(this.constPool)
							+ ", branch instr index: "
							+ codeEntry.getInstructionIndex() + ", target instr index: " + branchTarget
							+ ", code loop: " + codeLoop;
					if(branchTarget == codeLoop.getConditionStartInstrIndex()) {
						loopContinueInstrIndicesMap.put(codeEntry.getInstructionIndex(), codeLoop);
					} else if(branchTarget == codeLoop.getBreakTargetInstrIndex()) {
						loopBreakInstrIndicesMap.put(codeEntry.getInstructionIndex(), codeLoop);
					}
				}
			}
		}
		
		
//		// TODO - DEBUG - Analyse the branches/throws from the codeBlocks to determine how to detect if/for/while/switch statements later.
//		// TODO - Perhaps use Matlab to visualize this.
//		try {
//			java.io.File debugFile = new java.io.File("Debug branch method index - " + this.method.getNameIndex() + ".txt");
//			java.io.FileOutputStream outStream = new java.io.FileOutputStream(debugFile);
//			byte[][] branchTable = new byte[codeBlocks.size()][codeBlocks.size()];
//			for(int i = 0; i < branchTable.length; i++) {
//				for(int j = 0; j < branchTable.length; j++) {
//					branchTable[i][j] = 0; // Initialize.
//					
//					// Normal branching.
//					for(String branchToLabel : codeBlocks.get(i).getBranchLabels()) {
//						if(branchToLabel.equals(codeBlocks.get(j).getLabel())) {
//							branchTable[i][j] = 1;
//						}
//					}
//				}
//				
//				// Exception handlers (branching to catch blocks).
//				for(ExceptionHandler exHandler : codeBlocks.get(i).getExceptionHandlers()) {
//					String handlerLabel = labelMap.get(exHandler.getHandlerPc());
//					for(int j = 0; j < branchTable.length; j++) {
//						if(codeBlocks.get(j).getLabel().equals(handlerLabel)) {
//							branchTable[i][j] = 2;
//						}
//					}
//				}
//			}
//			String str = "branchTable = [";
//			for(int i = 0; i < branchTable.length; i++) {
//				str += "\n\t\t";
//				for(int j = 0; j < branchTable.length; j++) {
//					str += branchTable[i][j] + ",";
//				}
//				str = str.substring(0, str.length() - 1) + ";";
//			}
//			str = str.substring(0, str.length() - 1) + "\n\t];";
//			outStream.write(str.getBytes(java.nio.charset.StandardCharsets.UTF_8));
//			
////			for(CodeBlock codeBlock : codeBlocks) {
////				String str = codeBlock.getLabel() + " -->";
////				for(String branchToLabel : codeBlock.getBranchLabels()) {
////					str += " " + branchToLabel;
////				}
////				for(ExceptionHandler exHandler : codeBlock.getExceptionHandlers()) {
////					String handlerLabel = labelMap.get(exHandler.getHandlerPc());
////					str += " " + handlerLabel;
////				}
////				str += "\r\n";
////				outStream.write(str.getBytes(java.nio.charset.StandardCharsets.UTF_8));
////			}
//			outStream.flush();
//			outStream.close();
//			
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		// END OF DEBUG.
		
		
// Generates return String with code blocks in labels, including where the code blocks branch to.
//		// Generate a String to return (using CodeBlock objects).
//		String ret = "{";
//		if(codeBlocks.size() == 1) {
//			ret += codeBlocks.get(0).toDecompiledCodeString();
//		} else {
//			for(int i = 0; i < codeBlocks.size(); i++) {
//				CodeBlock codeBlock = codeBlocks.get(i);
//				String branchInfo = "";
//				for(CodeLabel label : codeBlock.getBranchLabels()) {
//					branchInfo += (branchInfo.isEmpty() ? " // " : ", ") + label.toString();
//				}
//				ret += "\n\t" + codeBlock.getLabel() + ": {" + branchInfo
//						+ "\n\t\t" + codeBlock.toDecompiledCodeString().replaceAll("\n", "\n\t\t") + "\n\t}";
//			}
//		}
//		ret += "\n}";
//		return ret;
		
		// Generate a String to return (using CodeBlock objects).
		String ret = "{";
		String tabs = "\t";
		ArrayList<Object> codeObjects = new ArrayList<Object>();
		ArrayList<CodeLabel> usedLabelMap = new ArrayList<CodeLabel>();
		for(int i = 0; i < codeBlocks.size(); i++) {
			CodeLabel label = codeBlocks.get(i).getLabel();
			
			// Skip code blocks that have no reference to them (CATCH blocks are not handled yet).
			boolean isReferencedTo = (i == 0);
			if(!isReferencedTo) {
				for(int j = 0; j < codeBlocks.size(); j++) {
					for(CodeLabel branchToLabel : codeBlocks.get(j).getBranchLabels()) {
						if(branchToLabel.equals(label)) {
							isReferencedTo = true;
							break;
						}
					}
				}
			}
			if(!isReferencedTo) {
				continue;
			}
			
			// Add a label scope closing "}" if the label was defined earlier.
			if(usedLabelMap.contains(label)) {
				tabs = tabs.substring(1);
				ret += "\n" + tabs + "}";
			}
			
			// Check if the code block is referenced to from a later index (detect loops).
			int lastRefIndex = -1;
			for(int j = i; j < codeBlocks.size(); j++) {
				for(CodeLabel branchToLabel : codeBlocks.get(j).getBranchLabels()) {
					if(branchToLabel.equals(label) && j > lastRefIndex) {
						lastRefIndex = j;
					}
				}
			}
			
			// Add a LOOP_Lxx label with a loop and add a label to break to if a loop was found.
			if(lastRefIndex != -1) {
				CodeLabel breakLabel = codeBlocks.get(lastRefIndex).getLabel();
				if(!usedLabelMap.contains(breakLabel)) {
					usedLabelMap.add(breakLabel);
					ret += "\n" + tabs + breakLabel.toString() + ":";
					
					int lastLowerLabelIndex = -1;
					for(int k = 0; k < codeObjects.size(); k++) {
						Object obj = codeObjects.get(k);
						if(obj instanceof CodeLabel) {
							CodeLabel label2 = (CodeLabel) obj;
							if(label2.getId() < breakLabel.getId()) {
								lastLowerLabelIndex = k;
								break;
							}
						}
					}
					if(lastLowerLabelIndex == -1) {
						codeObjects.add(breakLabel);
					} else {
						codeObjects.add(lastLowerLabelIndex, breakLabel);
					}
				}
				CodeLabel loopLabel = new CodeLabel(label.getId(), LabelType.LOOP_LABEL);
				codeObjects.add(loopLabel);
				ret += "\n" + tabs + loopLabel.toString() + ":\n" + tabs + "while(true) {";
				tabs += "\t";
				usedLabelMap.add(loopLabel);
				
			}
			
			// Add required labels to branch to if they do not yet exist. TODO - The labels have to be added around all lower existing labels to work.
			for(CodeLabel gotoLabel : codeBlocks.get(i).getBranchLabels()) {
				if(!usedLabelMap.contains(gotoLabel) && gotoLabel.getId() > i + 2) { // i starts at 0, labels at 1.
					
					int lastLowerLabelIndex = -1;
					for(int k = 0; k < codeObjects.size(); k++) {
						Object obj = codeObjects.get(k);
						if(obj instanceof CodeLabel) {
							CodeLabel label2 = (CodeLabel) obj;
							if(label2.getId() < gotoLabel.getId()) {
								lastLowerLabelIndex = k;
								break;
							}
						}
					}
					if(lastLowerLabelIndex == -1) {
						codeObjects.add(gotoLabel);
					} else {
						codeObjects.add(lastLowerLabelIndex, gotoLabel);
					}
					
					ret += "\n" + tabs + gotoLabel.toString() + ": {";
					tabs += "\t";
					usedLabelMap.add(gotoLabel);
				}
			}
			
			// Add the code. TODO - Replace labels somewhere else. Check label indices instead of strings.
			codeObjects.add(codeBlocks.get(i));
			String decompStr = codeBlocks.get(i).toDecompiledCodeString();
			Matcher matcher = Pattern.compile(" goto L([\\d]+)\\;").matcher(decompStr);
			if(matcher.find()) { // Only check one occurence.
				int labelIndex = Integer.parseInt(matcher.group(1));
				if(labelIndex > i) {
					decompStr = decompStr.substring(0, matcher.start()) + " break L" + labelIndex + ";"
							+ decompStr.substring(matcher.end());
				} else {
					decompStr = decompStr.substring(0, matcher.start()) + " continue LOOP_L" + labelIndex + ";"
							+ decompStr.substring(matcher.end());
				}
			}
			decompStr = decompStr.replaceAll(" goto ", " break ");
			
			ret += ("\n" + decompStr).replaceAll("\n", "\n" + tabs);
			
		}
		ret += "\n}";
		
		
		
		// TODO - LOOP labels are at the wrong location (should often be before some labels) and are not closed with a "}".
		// Form a code string from the labels and code.
		ret = "{";
		tabs = "\t";
		for(int k = 0; k < codeObjects.size(); k++) {
//			ret += "\n\n" + codeObjects.get(k).toString(); // DEBUG.
			
			Object obj = codeObjects.get(k);
			if(obj instanceof CodeLabel) {
				CodeLabel label = (CodeLabel) obj;
				switch(label.getType()) {
				case BRANCH_LABEL:
					ret += "\n" + tabs + label.toString() + ": {";
					tabs += "\t";
					break;
				case CATCH_LABEL:
					ret += "\n" + tabs + label.toString() + ": {";
					tabs += "\t";
//					throw new RuntimeException("CodeLabel type CATCH_LABEL has not been implemented.");
				case LOOP_LABEL:
					ret += "\n" + tabs + label.toString() + ":\n" + tabs + "while(true) {";
					tabs += "\t";
					break;
				default:
					throw new RuntimeException("Unexpected CodeLabel type: " + label.getType().toString());
				}
				
			} else if(obj instanceof CodeBlock) {
				CodeBlock codeBlock = (CodeBlock) obj;
				
				// Check if a CodeLabel that branches to this CodeBlock was found before this block.
				// Add a label close "}" if this is the case.
				for(int m = 0; m < k; m++) {
					Object obj2 = codeObjects.get(m);
					if(obj2 instanceof CodeLabel && ((CodeLabel) obj2).getId() == codeBlock.getLabel().getId()
							&& ((CodeLabel) obj2).getType() != LabelType.LOOP_LABEL) {
						tabs = tabs.substring(1);
						ret += "\n" + tabs + "}";
					}
				}
				
				// Add the code block, replacing "goto" instructions with "break" or "continue".
				String decompStr = codeBlock.toDecompiledCodeString();
				Matcher matcher = Pattern.compile(" goto L([\\d]+)\\;").matcher(decompStr); // TODO - Check if this format is right for LOOP_L labels.
				if(matcher.find()) { // Only check one occurence.
					int labelIndex = Integer.parseInt(matcher.group(1));
					if(labelIndex > codeBlock.getLabel().getId()) {
						decompStr = decompStr.substring(0, matcher.start()) + " break L" + labelIndex + ";"
								+ decompStr.substring(matcher.end());
					} else {
						decompStr = decompStr.substring(0, matcher.start()) + " continue LOOP_L" + labelIndex + ";"
								+ decompStr.substring(matcher.end());
					}
				}
//				decompStr = decompStr.replaceAll(" goto ", " break ");
				
				ret += ("\n" + decompStr).replaceAll("\n", "\n" + tabs);
				
				
			} else {
				throw new RuntimeException("Unexpected object type: " + obj.getClass().getName());
			}
		}
		ret += "\n}";
		
		return ret;
		
		
		
		
		
		
//		// Replace instructions with string representations.
//		Stack<CodeStackObject> stack = new Stack<CodeStackObject>();
//		LocalVariableGen localVarMap = new LocalVariableGen();
//		int localVarIndex = 0;
//		if(!this.isStatic) {
//			localVarMap.setVarName(localVarIndex++, "this");
//		}
//		for(int i = 0; i < methodArgSize; i++) {
//			localVarMap.setVarName(localVarIndex, "arg" + localVarIndex++);
//		}
//
//		String[] codeStrArray = new String[byteCodeEntries.size()];
//		try {
//			for(int i = 0; i < byteCodeEntries.size(); i++) {
//				codeStrArray[i] = null; // Initialize.
//				ByteCodeEntry instrEntry = byteCodeEntries.get(i);
//				ByteCodeInstruction instr = instrEntry.getInstruction();
//				int[] signedInstrArgs = instrEntry.getSignedInstructionArgs();
//				int[] unsignedInstrArgs = instrEntry.getUnsignedInstructionArgs();
//				
//				switch(instr) {
//				case nop: break;
//				case aconst_null: stack.push(new CodeStackNull(i)); break;
//				case iconst_m1: stack.push(new CodeStackConstValue(i, (int) -1)); break;
//				case iconst_0: stack.push(new CodeStackConstValue(i, (int) 0)); break;
//				case iconst_1: stack.push(new CodeStackConstValue(i, (int) 1)); break;
//				case iconst_2: stack.push(new CodeStackConstValue(i, (int) 2)); break;
//				case iconst_3: stack.push(new CodeStackConstValue(i, (int) 3)); break;
//				case iconst_4: stack.push(new CodeStackConstValue(i, (int) 4)); break;
//				case iconst_5: stack.push(new CodeStackConstValue(i, (int) 5)); break;
//				case lconst_0: stack.push(new CodeStackConstValue(i, (long) 0)); break;
//				case lconst_1: stack.push(new CodeStackConstValue(i, (long) 1)); break;
//				case fconst_0: stack.push(new CodeStackConstValue(i, (float) 0)); break;
//				case fconst_1: stack.push(new CodeStackConstValue(i, (float) 1)); break;
//				case fconst_2: stack.push(new CodeStackConstValue(i, (float) 2)); break;
//				case dconst_0: stack.push(new CodeStackConstValue(i, (double) 0)); break;
//				case dconst_1: stack.push(new CodeStackConstValue(i, (double) 1)); break;
//				case bipush: stack.push(new CodeStackConstValue(i, (int) signedInstrArgs[0])); break;
//				case sipush: stack.push(new CodeStackConstValue(i, (int) signedInstrArgs[0])); break;
//				case ldc: stack.push(new CodeStackConstValue(i,
//						this.constPool.get(unsignedInstrArgs[0]).simpleVal(this.constPool))); break; // Push String, int or float.
//				case ldc_w: stack.push(new CodeStackConstValue(i,
//						this.constPool.get(unsignedInstrArgs[0]).simpleVal(this.constPool))); break; // Push String, int or float.
//				case ldc2_w: stack.push(new CodeStackConstValue(i,
//						this.constPool.get(unsignedInstrArgs[0]).simpleVal(this.constPool))); break; // Push double or long.
//				case iload:
//				case lload:
//				case fload:
//				case dload:
//				case aload: 
//					stack.push(new CodeStackVariable(i, localVarMap.getVarName(unsignedInstrArgs[0])));
//					break;
//				case iload_0: stack.push(new CodeStackVariable(i, localVarMap.getVarName(0))); break;
//				case iload_1: stack.push(new CodeStackVariable(i, localVarMap.getVarName(1))); break;
//				case iload_2: stack.push(new CodeStackVariable(i, localVarMap.getVarName(2))); break;
//				case iload_3: stack.push(new CodeStackVariable(i, localVarMap.getVarName(3))); break;
//				case lload_0: stack.push(new CodeStackVariable(i, localVarMap.getVarName(0))); break;
//				case lload_1: stack.push(new CodeStackVariable(i, localVarMap.getVarName(1))); break;
//				case lload_2: stack.push(new CodeStackVariable(i, localVarMap.getVarName(2))); break;
//				case lload_3: stack.push(new CodeStackVariable(i, localVarMap.getVarName(3))); break;
//				case fload_0: stack.push(new CodeStackVariable(i, localVarMap.getVarName(0))); break;
//				case fload_1: stack.push(new CodeStackVariable(i, localVarMap.getVarName(1))); break;
//				case fload_2: stack.push(new CodeStackVariable(i, localVarMap.getVarName(2))); break;
//				case fload_3: stack.push(new CodeStackVariable(i, localVarMap.getVarName(3))); break;
//				case dload_0: stack.push(new CodeStackVariable(i, localVarMap.getVarName(0))); break;
//				case dload_1: stack.push(new CodeStackVariable(i, localVarMap.getVarName(1))); break;
//				case dload_2: stack.push(new CodeStackVariable(i, localVarMap.getVarName(2))); break;
//				case dload_3: stack.push(new CodeStackVariable(i, localVarMap.getVarName(3))); break;
//				case aload_0: stack.push(new CodeStackVariable(i, localVarMap.getVarName(0))); break;
//				case aload_1: stack.push(new CodeStackVariable(i, localVarMap.getVarName(1))); break;
//				case aload_2: stack.push(new CodeStackVariable(i, localVarMap.getVarName(2))); break;
//				case aload_3: stack.push(new CodeStackVariable(i, localVarMap.getVarName(3))); break;
//				case iaload:
//				case laload:
//				case faload:
//				case daload:
//				case aaload:
//				case baload:
//				case caload:
//				case saload: {
//					String arrayIndex = stack.pop().getDecompStr();
//					String arrayRef = stack.pop().getDecompStr().replace('/', '.');
//					stack.push(new CodeStackCode(i, arrayRef + "[" + arrayIndex + "]"));
//					break;
//				}
//				case istore:
//				case lstore:
//				case fstore:
//				case dstore:
//				case astore:
//					codeStrArray[i] = localVarMap.getVarName(unsignedInstrArgs[0]) + " = " + stack.pop().getDecompStr() + ";";
//					break;
//				case istore_0: codeStrArray[i] = localVarMap.getVarName(0) + " = " + stack.pop().getDecompStr() + ";"; break;
//				case istore_1: codeStrArray[i] = localVarMap.getVarName(1) + " = " + stack.pop().getDecompStr() + ";"; break;
//				case istore_2: codeStrArray[i] = localVarMap.getVarName(2) + " = " + stack.pop().getDecompStr() + ";"; break;
//				case istore_3: codeStrArray[i] = localVarMap.getVarName(3) + " = " + stack.pop().getDecompStr() + ";"; break;
//				case lstore_0: codeStrArray[i] = localVarMap.getVarName(0) + " = " + stack.pop().getDecompStr() + ";"; break;
//				case lstore_1: codeStrArray[i] = localVarMap.getVarName(1) + " = " + stack.pop().getDecompStr() + ";"; break;
//				case lstore_2: codeStrArray[i] = localVarMap.getVarName(2) + " = " + stack.pop().getDecompStr() + ";"; break;
//				case lstore_3: codeStrArray[i] = localVarMap.getVarName(3) + " = " + stack.pop().getDecompStr() + ";"; break;
//				case fstore_0: codeStrArray[i] = localVarMap.getVarName(0) + " = " + stack.pop().getDecompStr() + ";"; break;
//				case fstore_1: codeStrArray[i] = localVarMap.getVarName(1) + " = " + stack.pop().getDecompStr() + ";"; break;
//				case fstore_2: codeStrArray[i] = localVarMap.getVarName(2) + " = " + stack.pop().getDecompStr() + ";"; break;
//				case fstore_3: codeStrArray[i] = localVarMap.getVarName(3) + " = " + stack.pop().getDecompStr() + ";"; break;
//				case dstore_0: codeStrArray[i] = localVarMap.getVarName(0) + " = " + stack.pop().getDecompStr() + ";"; break;
//				case dstore_1: codeStrArray[i] = localVarMap.getVarName(1) + " = " + stack.pop().getDecompStr() + ";"; break;
//				case dstore_2: codeStrArray[i] = localVarMap.getVarName(2) + " = " + stack.pop().getDecompStr() + ";"; break;
//				case dstore_3: codeStrArray[i] = localVarMap.getVarName(3) + " = " + stack.pop().getDecompStr() + ";"; break;
//				case astore_0: codeStrArray[i] = localVarMap.getVarName(0) + " = " + stack.pop().getDecompStr() + ";"; break;
//				case astore_1: codeStrArray[i] = localVarMap.getVarName(1) + " = " + stack.pop().getDecompStr() + ";"; break;
//				case astore_2: codeStrArray[i] = localVarMap.getVarName(2) + " = " + stack.pop().getDecompStr() + ";"; break;
//				case astore_3: codeStrArray[i] = localVarMap.getVarName(3) + " = " + stack.pop().getDecompStr() + ";"; break;
//				case iastore:
//				case lastore:
//				case fastore:
//				case dastore:
//				case aastore:
//				case bastore:
//				case castore:
//				case sastore: {
//					String value = stack.pop().getDecompStr();
//					String arrayIndex = stack.pop().getDecompStr();
//					String arrayRef = stack.pop().getDecompStr().replace('/', '.');
//					codeStrArray[i] = arrayRef + "[" + arrayIndex + "] = " + value;
//					break;
//				}
//				case pop: stack.pop(); break;
//				case pop2: throw new DecompileException("Found unsupported instruction: pop2 (Pops 1 or 2 items off the stack based on datatype)");
//				case dup: stack.push(stack.peek()); break;
//				case dup_x1: {
//					CodeStackObject topObj = stack.pop();
//					CodeStackObject secondObj = stack.pop();
//					stack.push(topObj);
//					stack.push(secondObj);
//					stack.push(topObj);
//					break;
//				}
//				case dup_x2: {
//					CodeStackObject topObj = stack.pop();
//					CodeStackObject secondObj = stack.pop();
//					CodeStackObject thirdObj = stack.pop();
//					stack.push(topObj);
//					stack.push(thirdObj);
//					stack.push(secondObj);
//					stack.push(topObj);
//					break;
//				}
//				case dup2: throw new DecompileException("Found unsupported instruction: dup2_x1 (Duplicates 1 or 2 items on the stack based on datatype)");
//				case dup2_x1: throw new DecompileException("Found unsupported instruction: dup2_x1 (Duplicates 1 or 2 items on the stack based on datatype)");
//				case dup2_x2: throw new DecompileException("Found unsupported instruction: dup2_x1 (Duplicates 1 or 2 items on the stack based on datatype)");
//				case swap: {
//					CodeStackObject topObj = stack.pop();
//					CodeStackObject secondObj = stack.pop();
//					stack.push(topObj);
//					stack.push(secondObj);
//					break;
//				}
//				case iadd:
//				case ladd:
//				case fadd:
//				case dadd:
//					stack.push(new CodeStackCode(i, "(" + stack.pop().getDecompStr() + ") + (" + stack.pop().getDecompStr() + ")"));
//					break;
//				case isub:
//				case lsub:
//				case fsub:
//				case dsub:
//					stack.push(new CodeStackCode(i, "(" + stack.pop().getDecompStr() + ") - (" + stack.pop().getDecompStr() + ")"));
//					break;
//				case imul:
//				case lmul:
//				case fmul:
//				case dmul:
//					stack.push(new CodeStackCode(i, "(" + stack.pop().getDecompStr() + ") * (" + stack.pop().getDecompStr() + ")"));
//					break;
//				case idiv:
//				case ldiv:
//				case fdiv:
//				case ddiv:
//					stack.push(new CodeStackCode(i, "(" + stack.pop().getDecompStr() + ") / (" + stack.pop().getDecompStr() + ")"));
//					break;
//				case irem:
//				case lrem:
//				case frem: // TODO - Check if "float % float" is valid.
//				case drem: // TODO - Check if "double % double" is valid.
//					stack.push(new CodeStackCode(i, "(" + stack.pop().getDecompStr() + ") % (" + stack.pop().getDecompStr() + ")"));
//					break;
//				case ineg:
//				case lneg:
//				case fneg:
//				case dneg:
//					stack.push(new CodeStackCode(i, "- " + stack.pop().getDecompStr()));
//					break;
//				case ishl:
//				case lshl: {
//					String shiftAmount = stack.pop().getDecompStr();
//					String value = stack.pop().getDecompStr();
//					stack.push(new CodeStackCode(i, "(" + value + ") << (" + shiftAmount + ")"));
//					break;
//				}
//				case ishr:
//				case lshr: {
//					String shiftAmount = stack.pop().getDecompStr();
//					String value = stack.pop().getDecompStr();
//					stack.push(new CodeStackCode(i, "(" + value + ") >> (" + shiftAmount + ")"));
//					break;
//				}
//				case iushr:
//				case lushr: {
//					String shiftAmount = stack.pop().getDecompStr();
//					String value = stack.pop().getDecompStr();
//					stack.push(new CodeStackCode(i, "(" + value + ") >>> (" + shiftAmount + ")"));
//					break;
//				}
//				case iand: stack.push(new CodeStackCode(i, "(" + stack.pop().getDecompStr() + ") & (" + stack.pop().getDecompStr() + ")")); break;
//				case land: stack.push(new CodeStackCode(i, "(" + stack.pop().getDecompStr() + ") & (" + stack.pop().getDecompStr() + ")")); break;
//				case ior: stack.push(new CodeStackCode(i, "(" + stack.pop().getDecompStr() + ") | (" + stack.pop().getDecompStr() + ")")); break;
//				case lor: stack.push(new CodeStackCode(i, "(" + stack.pop().getDecompStr() + ") | (" + stack.pop().getDecompStr() + ")")); break;
//				case ixor: stack.push(new CodeStackCode(i, "(" + stack.pop().getDecompStr() + ") ^ (" + stack.pop().getDecompStr() + ")")); break;
//				case lxor: stack.push(new CodeStackCode(i, "(" + stack.pop().getDecompStr() + ") ^ (" + stack.pop().getDecompStr() + ")")); break;
//				case iinc: codeStrArray[i] = localVarMap.getVarName(unsignedInstrArgs[0]) + "+= " + signedInstrArgs[1] + ";"; break;
//				case i2l: stack.push(new CodeStackCode(i, "(long) (" + stack.pop().getDecompStr() + ")")); break;
//				case i2f: stack.push(new CodeStackCode(i, "(float) (" + stack.pop().getDecompStr() + ")")); break;
//				case i2d: stack.push(new CodeStackCode(i, "(double) (" + stack.pop().getDecompStr() + ")")); break;
//				case l2i: stack.push(new CodeStackCode(i, "(int) (" + stack.pop().getDecompStr() + ")")); break;
//				case l2f: stack.push(new CodeStackCode(i, "(float) (" + stack.pop().getDecompStr() + ")")); break;
//				case l2d: stack.push(new CodeStackCode(i, "(double) (" + stack.pop().getDecompStr() + ")")); break;
//				case f2i: stack.push(new CodeStackCode(i, "(int) (" + stack.pop().getDecompStr() + ")")); break;
//				case f2l: stack.push(new CodeStackCode(i, "(long) (" + stack.pop().getDecompStr() + ")")); break;
//				case f2d: stack.push(new CodeStackCode(i, "(double) (" + stack.pop().getDecompStr() + ")")); break;
//				case d2i: stack.push(new CodeStackCode(i, "(int) (" + stack.pop().getDecompStr() + ")")); break;
//				case d2l: stack.push(new CodeStackCode(i, "(long) (" + stack.pop().getDecompStr() + ")")); break;
//				case d2f: stack.push(new CodeStackCode(i, "(float) (" + stack.pop().getDecompStr() + ")")); break;
//				case i2b: stack.push(new CodeStackCode(i, "(byte) (" + stack.pop().getDecompStr() + ")")); break;
//				case i2c: stack.push(new CodeStackCode(i, "(char) (" + stack.pop().getDecompStr() + ")")); break;
//				case i2s: stack.push(new CodeStackCode(i, "(short) (" + stack.pop().getDecompStr() + ")")); break;
//				case lcmp: throw new DecompileException("Found unsupported instruction: " + instr.getInstructionName());
//				case fcmpl: throw new DecompileException("Found unsupported instruction: " + instr.getInstructionName());
//				case fcmpg: throw new DecompileException("Found unsupported instruction: " + instr.getInstructionName());
//				case dcmpl: throw new DecompileException("Found unsupported instruction: " + instr.getInstructionName());
//				case dcmpg: throw new DecompileException("Found unsupported instruction: " + instr.getInstructionName());
//				case ifeq: codeStrArray[i] = "if((" + stack.pop().getDecompStr() + ") == 0) { goto " + labelMap.get(this.getInstrIndexForBranch(byteCodeEntries, instrEntry)) + " }"; break;
//				case ifne: codeStrArray[i] = "if((" + stack.pop().getDecompStr() + ") != 0) { goto " + labelMap.get(this.getInstrIndexForBranch(byteCodeEntries, instrEntry)) + " }"; break;
//				case iflt: codeStrArray[i] = "if((" + stack.pop().getDecompStr() + ") > 0) { goto " + labelMap.get(this.getInstrIndexForBranch(byteCodeEntries, instrEntry)) + " }"; break;
//				case ifge: codeStrArray[i] = "if((" + stack.pop().getDecompStr() + ") <= 0) { goto " + labelMap.get(this.getInstrIndexForBranch(byteCodeEntries, instrEntry)) + " }"; break;
//				case ifgt: codeStrArray[i] = "if((" + stack.pop().getDecompStr() + ") < 0) { goto " + labelMap.get(this.getInstrIndexForBranch(byteCodeEntries, instrEntry)) + " }"; break;
//				case ifle: codeStrArray[i] = "if((" + stack.pop().getDecompStr() + ") >= 0) { goto " + labelMap.get(this.getInstrIndexForBranch(byteCodeEntries, instrEntry)) + " }"; break;
//				case if_icmpeq: codeStrArray[i] = "if((" + stack.pop().getDecompStr() + ") == (" + stack.pop().getDecompStr() + ")) { goto " + labelMap.get(this.getInstrIndexForBranch(byteCodeEntries, instrEntry)) + " }"; break;
//				case if_icmpne: codeStrArray[i] = "if((" + stack.pop().getDecompStr() + ") != (" + stack.pop().getDecompStr() + ")) { goto " + labelMap.get(this.getInstrIndexForBranch(byteCodeEntries, instrEntry)) + " }"; break;
//				case if_icmplt: codeStrArray[i] = "if((" + stack.pop().getDecompStr() + ") > (" + stack.pop().getDecompStr() + ")) { goto " + labelMap.get(this.getInstrIndexForBranch(byteCodeEntries, instrEntry)) + " }"; break;
//				case if_icmpge: codeStrArray[i] = "if((" + stack.pop().getDecompStr() + ") <= (" + stack.pop().getDecompStr() + ")) { goto " + labelMap.get(this.getInstrIndexForBranch(byteCodeEntries, instrEntry)) + " }"; break;
//				case if_icmpgt: codeStrArray[i] = "if((" + stack.pop().getDecompStr() + ") < (" + stack.pop().getDecompStr() + ")) { goto " + labelMap.get(this.getInstrIndexForBranch(byteCodeEntries, instrEntry)) + " }"; break;
//				case if_icmple: codeStrArray[i] = "if((" + stack.pop().getDecompStr() + ") >= (" + stack.pop().getDecompStr() + ")) { goto " + labelMap.get(this.getInstrIndexForBranch(byteCodeEntries, instrEntry)) + " }"; break;
//				case if_acmpeq: codeStrArray[i] = "if((" + stack.pop().getDecompStr() + ") == (" + stack.pop().getDecompStr() + ")) { goto " + labelMap.get(this.getInstrIndexForBranch(byteCodeEntries, instrEntry)) + " }"; break;
//				case if_acmpne: codeStrArray[i] = "if((" + stack.pop().getDecompStr() + ") != (" + stack.pop().getDecompStr() + ")) { goto " + labelMap.get(this.getInstrIndexForBranch(byteCodeEntries, instrEntry)) + " }"; break;
//				case _goto: codeStrArray[i] = "goto " + labelMap.get(this.getInstrIndexForBranch(byteCodeEntries, instrEntry)) + ";"; break;
//				case jsr: throw new DecompileException("Found unsupported instruction: " + instr.getInstructionName());
//				case ret: throw new DecompileException("Found unsupported instruction: " + instr.getInstructionName());
//				case tableswitch: throw new DecompileException("Found unsupported instruction: " + instr.getInstructionName());
//				case lookupswitch: throw new DecompileException("Found unsupported instruction: " + instr.getInstructionName());
//				case ireturn:
//				case lreturn:
//				case freturn:
//				case dreturn:
//				case areturn: {
//					String returnStr = stack.pop().getDecompStr();
//					while(!stack.empty()) {
//						CodeStackObject obj = stack.pop();
//						int instrIndex = obj.getInstructionIndex();
//						if(codeStrArray[instrIndex] == null) {
//							codeStrArray[instrIndex] = obj.getDecompStr();
//						} else {
//							codeStrArray[instrIndex] += " /* Same line */ " + obj.getDecompStr();
//						}
//					}
//					codeStrArray[i] = "return " + returnStr + ";";
//					break;
//				}
//				case _return: {
//					while(!stack.empty()) {
//						CodeStackObject obj = stack.pop();
//						int instrIndex = obj.getInstructionIndex();
//						if(codeStrArray[instrIndex] == null) {
//							codeStrArray[instrIndex] = obj.getDecompStr();
//						} else {
//							codeStrArray[instrIndex] += " /* Same line */ " + obj.getDecompStr();
//						}
//					}
//					codeStrArray[i] = "return;";
//					break;
//				}
//				case getstatic: { // Get static Field value.
//					FieldRefDecoder fieldDecoder = new FieldRefDecoder(this.constPool, unsignedInstrArgs[0]);
//					stack.push(new CodeStackCode(i, "(" + fieldDecoder.getFieldTypeDesc() + ")" + fieldDecoder.getClassName() + "." + fieldDecoder.getFieldName()));
//					break;
//				}
//				case putstatic: { // Set static Field value.
//					FieldRefDecoder fieldDecoder = new FieldRefDecoder(this.constPool, unsignedInstrArgs[0]);
//					codeStrArray[i] = fieldDecoder.getClassName()
//							+ "." + fieldDecoder.getFieldName() + " = " + stack.pop().getDecompStr() + ";";
//					break;
//				}
//				case getfield: { // Get instance Field value.
//					FieldRefDecoder fieldDecoder = new FieldRefDecoder(this.constPool, unsignedInstrArgs[0]);
//					stack.push(new CodeStackCode(i, "(" + fieldDecoder.getFieldTypeDesc() + ") (" + stack.pop().getDecompStr() + ")." + fieldDecoder.getFieldName()));
//					break;
//				}
//				case putfield: { // Set instance Field value.
//					String value = stack.pop().getDecompStr();
//					String objRef = stack.pop().getDecompStr();
//					FieldRefDecoder fieldDecoder = new FieldRefDecoder(this.constPool, unsignedInstrArgs[0]);
//					codeStrArray[i] = "(" + objRef + ")." + fieldDecoder.getFieldName() + " = " + value + ";";
//					break;
//				}
//				case invokevirtual:
//				case invokespecial: {
//					MethodRefDecoder methodDecoder = new MethodRefDecoder(this.constPool, unsignedInstrArgs[0]);
//					String methodArgs = "";
//					for(int argNum = 0; argNum < methodDecoder.getMethodArgSize(); argNum++) {
//						methodArgs = stack.pop().getDecompStr() + (methodArgs.isEmpty() ? "" : ", " + methodArgs);
//					}
//					String objRef = stack.pop().getDecompStr();
//					if(methodDecoder.getMethodReturnTypeDesc().equals("V")) {
//						codeStrArray[i] = "(" +  objRef + ")." + methodDecoder.getMethodName() + "(" + methodArgs + ")";
//					} else {
//						stack.push(new CodeStackCode(i, "(" +  objRef + ")." + methodDecoder.getMethodName() + "(" + methodArgs + ")"));
//					}
//					break;
//				}
//				case invokestatic: {
//					MethodRefDecoder methodDecoder = new MethodRefDecoder(this.constPool, unsignedInstrArgs[0]);
//					String methodArgs = "";
//					for(int argNum = 0; argNum < methodDecoder.getMethodArgSize(); argNum++) {
//						methodArgs = stack.pop().getDecompStr() + (methodArgs.isEmpty() ? "" : ", " + methodArgs);
//					}
//					if(methodDecoder.getMethodReturnTypeDesc().equals("V")) {
//						codeStrArray[i] = methodDecoder.getClassName() + "." + methodDecoder.getMethodName() + "(" + methodArgs + ")";
//					} else {
//						stack.push(new CodeStackCode(i, "" +  methodDecoder.getClassName() + "." + methodDecoder.getMethodName() + "(" + methodArgs + ")"));
//					}
//					break;
//				}
//				case invokeinterface: {
//					MethodRefDecoder methodDecoder = new MethodRefDecoder(this.constPool, unsignedInstrArgs[0]);
//					String methodArgs = "";
//					for(int argNum = 0; argNum < methodDecoder.getMethodArgSize(); argNum++) {
//						methodArgs = stack.pop().getDecompStr() + (methodArgs.isEmpty() ? "" : ", " + methodArgs);
//					}
//					String objRef = stack.pop().getDecompStr();
//					if(methodDecoder.getMethodReturnTypeDesc().equals("V")) {
//						codeStrArray[i] = "(" +  objRef + ")." + methodDecoder.getMethodName() + "(" + methodArgs + ")";
//					} else {
//						stack.push(new CodeStackCode(i, "(" +  objRef + ")." + methodDecoder.getMethodName() + "(" + methodArgs + ")"));
//					}
//					break;
//				}
//				case invokedynamic: {
//					MethodRefDecoder methodDecoder = new MethodRefDecoder(this.constPool, unsignedInstrArgs[0]);
//					String methodArgs = "";
//					for(int argNum = 0; argNum < methodDecoder.getMethodArgSize(); argNum++) {
//						methodArgs = stack.pop().getDecompStr() + (methodArgs.isEmpty() ? "" : ", " + methodArgs);
//					}
//					if(methodDecoder.getMethodReturnTypeDesc().equals("V")) {
//						codeStrArray[i] = methodDecoder.getClassName() + "." + methodDecoder.getMethodName() + "(" + methodArgs + ")";
//					} else {
//						stack.push(new CodeStackCode(i, methodDecoder.getClassName() + "." + methodDecoder.getMethodName() + "(" + methodArgs + ")"));
//					}
//					break;
//				}
//				case _new: {
//					ClassRefDecoder classDecoder = new ClassRefDecoder(this.constPool, unsignedInstrArgs[0]);
//					stack.push(new CodeStackCode(i, "new " + classDecoder.getClassName())); // This has to be completed with an invokespecial of the <init>(args...) method.
//					break;
//				}
////				case newarray: break; // TODO - Implement instruction.
////				case anewarray: break; // TODO - Implement instruction.
////				case arraylength: break; // TODO - Implement instruction.
////				case athrow: break; // TODO - Implement instruction.
////				case checkcast: break; // TODO - Implement instruction.
////				case _instanceof: break; // TODO - Implement instruction.
////				case monitorenter: break;
////				case monitorexit: break;
////				case wide: break;
//				case multianewarray: {
//					ClassRefDecoder classDecoder = new ClassRefDecoder(this.constPool, unsignedInstrArgs[0]);
//					String codeStr = "";
//					for(int j = 0; j < classDecoder.getArrayDim(); j++) {
//						codeStr = "[" + stack.pop().getDecompStr() + "]" + codeStr;
//					}
//					codeStr = "new " + classDecoder.getClassName().replaceAll("\\[\\]", "") + codeStr;
//					stack.push(new CodeStackCode(i, codeStr));
//					break;
//				}
//				case ifnull: codeStrArray[i] = "if((" + stack.pop().getDecompStr() + ") == null) { goto " + labelMap.get(i) + " }"; break;
//				case ifnonnull: codeStrArray[i] = "if((" + stack.pop().getDecompStr() + ") != null) { goto " + labelMap.get(i) + " }"; break;
//				case goto_w: codeStrArray[i] = "goto " + labelMap.get(i) + ";"; break;
//				case jsr_w: throw new DecompileException("Found unsupported instruction: " + instr.getInstructionName());
//				case breakpoint: throw new DecompileException("Found unsupported instruction: " + instr.getInstructionName());
//				case impdep1: throw new DecompileException("Found unsupported instruction: " + instr.getInstructionName());
//				case impdep2: throw new DecompileException("Found unsupported instruction: " + instr.getInstructionName());
//				default:
//					throw new DecompileException("No handling code found for instruction: " + instr.getInstructionName());
//				}
//				
////				// TODO - Remove DEBUG.
////				if(codeStrArray[i] != null) {
////					System.out.println(codeStrArray[i]);
////				}
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		
//		// Append unused stack objects to see if code was skipped.
//		while(!stack.isEmpty()) {
//			CodeStackObject unhandledObject = stack.pop();
//			int instrIndex = unhandledObject.getInstructionIndex();
//			if(codeStrArray[instrIndex] == null) {
//				codeStrArray[instrIndex] = "// UNHANDLED STACK OBJECT: " + unhandledObject.getDecompStr().replaceAll("(\r|\n)", "~NEWLINE");
//			} else {
//				codeStrArray[instrIndex] += " // UNHANDLED STACK OBJECT: " + unhandledObject.getDecompStr().replaceAll("(\r|\n)", "~NEWLINE");
//			}
//		}
//		
//		// Generate the code string to return.
//		String ret = "{";
//		for(int i = 0; i < codeStrArray.length; i++) {
//			
//			// Add a label start if there is one at this index.
//			if(labelMap.containsKey(i)) {
//				ret += "\n\t" + labelMap.get(i) + " {";
//			}
//			
//			// Add the code.
//			if(codeStrArray[i] != null) {
//				ret += "\n\t\t/*" + String.format("%3d", i) + " */ " + codeStrArray[i];
//			}
//			
//			// Add a label end if there is one at the next index.
//			if(labelMap.containsKey(i + 1)) {
//				ret += "\n\t}";
//			}
//			
//		}
//		ret += "\n}";
		
		
		
		
		
		
		
//		// Generate a String to return.
//		String ret = "{";
//		String currentLabel = null;
//		for(int i = 0; i < byteCodeEntries.size(); i++) {
//			
//			// Add a label start if there is one at this index.
//			if(labelMap.containsKey(i)) {
//				ret += "\n\t" + labelMap.get(i) + " {";
//				currentLabel = labelMap.get(i);
//			}
//			
//			// Add the instruction.
//			ByteCodeEntry codeEntry = byteCodeEntries.get(i);
//			ByteCodeInstruction instr = codeEntry.getInstruction();
//			int[] signedInstrArgs = codeEntry.getSignedInstructionArgs();
//			int[] unsignedInstrArgs = codeEntry.getUnsignedInstructionArgs();
//			
//			String argsStr = "";
//			if(instr.isBranchInstruction()) {
//				if(signedInstrArgs.length != 1) {
//					throw new DecompileException("Expecting branch instruction " + instr.getInstructionName()
//							+ " to have argument size 1. Size was: " + codeEntry.getSignedInstructionArgs().length);
//				}
//				int branchOffset = signedInstrArgs[0];
//				int absoluteIndex = codeEntry.getOffset() + branchOffset;
//				
//				searchLoop: {
//					for(int j = 0; j < byteCodeEntries.size(); j++) {
//						if(byteCodeEntries.get(j).getOffset() == absoluteIndex) {
//							String targetLabel = labelMap.get(j);
//							argsStr += " branch-> " + targetLabel;
////							argsStr += " branch-> #" + j + "(" + ((j - i) >= 0 ? "+" : "") + (j - i) + ") ("
////									+ byteCodeEntries.get(j).getInstruction().getInstructionName() + ")";
//							
////							if(targetLabel.equals(currentLabel)) { // TODO - Check if this is indeed an error. <-- It's not.
//////								System.out.println("[DEBUG]" + "Branch instruction is branching to its own code block at instr index: " + j
//////										+ " (" + instr.getInstructionName() + ")");
////								throw new DecompileException("Branch instruction is branching to its own code block at instr index: " + i
////										+ " (" + instr.getInstructionName() + ", label=" + currentLabel + ")");
////							}
//							break searchLoop;
//						}
//					}
//					throw new DecompileException("Branch instruction " + instr.getInstructionName()
//							+ "(instruction index #" + i + ") pointed to an unexisting bytecode instruction index. Relative: "
//							+ branchOffset + " / Absolute: " + absoluteIndex);
//				}
//				
//			} else {
//				for(int j = 0; j < instr.getPayloadTypes().length; j++) {
//					ByteCodeInstructionPayload argType = instr.getPayloadTypes()[j];
//					int signedInstrArg = signedInstrArgs[j];
//					int unsignedInstrArg = unsignedInstrArgs[j];
//					
//					if(argType.isConstPoolIndex()) {
//						argsStr += (argsStr.isEmpty() ? " " : ", ") + this.constPool.get(unsignedInstrArg).simpleVal(this.constPool);
//					} else {
//						argsStr += (argsStr.isEmpty() ? " " : ", ") + signedInstrArg;
//					}
//				}
//			}
//			
//			ret += "\n\t\t" + instr.getInstructionName() + argsStr;
//			
//			// Add a label end if there is one at the next index.
//			if(labelMap.containsKey(i + 1)) {
//				ret += "\n\t}";
//				currentLabel = null;
//			}
//		}
//		ret += "\n}";		
		
		
		
		
		
		
		
		
		
//		// Generate a String to return (switch/case representation test).
//		String ret = "{"
//				+ "\n\tString label = \"" + labelMap.get(0) + "\";"
//				+ "\n\twhile(true) {"
//				+ "\n\t\tswitch(label) {";
//		for(int i = 0; i < byteCodeEntries.size(); i++) {
//			
//			// Add a label start if there is one at this index.
//			if(labelMap.containsKey(i)) {
//				ret += "\n\t\tcase \"" + labelMap.get(i) + "\":";
//			}
//			
//			// Add the instruction.
//			ByteCodeEntry codeEntry = byteCodeEntries.get(i);
//			ByteCodeInstruction instr = codeEntry.getInstruction();
//			int[] signedInstrArgs = codeEntry.getSignedInstructionArgs();
//			int[] unsignedInstrArgs = codeEntry.getUnsignedInstructionArgs();
//			
//			String argsStr = "";
//			if(instr.isBranchInstruction()) {
//				if(signedInstrArgs.length != 1) {
//					throw new DecompileException("Expecting branch instruction " + instr.getInstructionName()
//							+ " to have argument size 1. Size was: " + codeEntry.getSignedInstructionArgs().length);
//				}
//				int branchOffset = signedInstrArgs[0];
//				int absoluteIndex = codeEntry.getOffset() + branchOffset;
//				
//				searchLoop: {
//					for(int j = 0; j < byteCodeEntries.size(); j++) {
//						if(byteCodeEntries.get(j).getOffset() == absoluteIndex) {
//							String targetLabel = labelMap.get(j);
//							argsStr += " branch-> " + targetLabel;
//							break searchLoop;
//						}
//					}
//					throw new DecompileException("Branch instruction " + instr.getInstructionName()
//							+ "(instruction index #" + i + ") pointed to an unexisting bytecode instruction index. Relative: "
//							+ branchOffset + " / Absolute: " + absoluteIndex);
//				}
//				
//			} else {
//				for(int j = 0; j < instr.getPayloadTypes().length; j++) {
//					ByteCodeInstructionPayload argType = instr.getPayloadTypes()[j];
//					int signedInstrArg = signedInstrArgs[j];
//					int unsignedInstrArg = unsignedInstrArgs[j];
//					
//					if(argType.isConstPoolIndex()) {
//						argsStr += (argsStr.isEmpty() ? " " : ", ") + this.constPool.get(unsignedInstrArg).simpleVal(this.constPool);
//					} else {
//						argsStr += (argsStr.isEmpty() ? " " : ", ") + signedInstrArg;
//					}
//				}
//			}
//			
//			ret += "\n\t\t\t" + instr.getInstructionName() + argsStr;
//			
//			// Add a label end if there is one at the next index.
////			if(labelMap.containsKey(i + 1)) {
////				ret +="\n\t\t\tbreak;";
////			}
//		}
//		ret += "\n\t\t}"
//				+ "\n\t}"
//				+ "\n}";
//		
//		return ret;
	}
	
	private List<ExceptionHandler> getExceptionHandlers(
			ExceptionTable exceptionTable, List<ByteCodeEntry> byteCodeEntries) throws DecompileException {
		ArrayList<ExceptionHandler> exceptionHandlers = new ArrayList<ExceptionHandler>(); 
		for(ExceptionTableEntry exEntry : exceptionTable.getTableEntries()) {
			
			// Get the Exception type.
			int catchTypeIndex = exEntry.getCatchTypeIndex();
			if(!this.constPool.hasIndex(catchTypeIndex)) {
				throw new DecompileException("ExceptionType in an ExceptionTableEntry"
						+ " points to an unexisting ClassConstantPool index: " + catchTypeIndex);
			}
			ConstantPoolObject catchTypeObj = this.constPool.get(catchTypeIndex);
			if(!(catchTypeObj instanceof ConstantPoolClassRef)) {
				throw new DecompileException("ExceptionType in an ExceptionTableEntry is expected to point at "
						+ ConstantPoolClassRef.class.getSimpleName() + " but found " + catchTypeObj.getClass().getSimpleName() + ".");
			}
			int catchTypeNameIndex = ((ConstantPoolClassRef) catchTypeObj).getIndex();
			if(!this.constPool.hasIndex(catchTypeNameIndex)) {
				throw new DecompileException("ExceptionType in an ExceptionTableEntry"
						+ " points to a ConstantPoolClassRef that points to an unexisting ClassConstantPool index: " + catchTypeNameIndex);
			}
			ConstantPoolObject catchTypeNameObj = this.constPool.get(catchTypeNameIndex);
			if(!(catchTypeNameObj instanceof ConstantPoolString)) {
				throw new DecompileException("ConstantPoolClassRef (for ExceptionType in an ExceptionTableEntry) is expected to point at "
						+ ConstantPoolString.class.getSimpleName() + " but found " + catchTypeNameObj.getClass().getSimpleName() + ".");
			}
			String exceptionType = ((ConstantPoolString) catchTypeNameObj).getString().replace('/', '.');
			
			// Get the absolute instruction indices from the given bytecode indices.
			int startPc = exEntry.getStartPc();
			int endPc = exEntry.getEndPc();
			int handlerPc = exEntry.getHandlerPc();
			
			int startPcIndex = -1;
			int endPcIndex = -1;
			int handlerPcIndex = -1;
			for(int i = 0; i < byteCodeEntries.size(); i++) {
				ByteCodeEntry codeEntry = byteCodeEntries.get(i);
				if(codeEntry.getOffset() == startPc) {
					startPcIndex = i;
				}
				if(codeEntry.getOffset() == endPc) {
					endPcIndex = i;
				}
				if(codeEntry.getOffset() == handlerPc) {
					handlerPcIndex = i;
				}
				if(startPcIndex != -1 && endPcIndex != -1 && handlerPcIndex != -1) {
					break;
				}
			}
			if(startPcIndex == -1 || endPcIndex == -1 || handlerPcIndex == -1) {
				throw new DecompileException("Invalid ExceptionTable entry: {start_pc=" + startPc
						+ ", end_pc=" + endPc + ", handler_pc=" + handlerPc + ", exception_type=" + exceptionType + "}");
			}
			if(endPcIndex <= startPcIndex) {
				throw new DecompileException("Invalid ExceptionTable entry (end_pc <= start_pc): {start_pc=" + startPc
						+ ", end_pc=" + endPc + ", handler_pc=" + handlerPc + ", exception_type=" + exceptionType + "}");
			}
			if(handlerPcIndex < endPcIndex) {
				throw new DecompileException("Invalid ExceptionTable entry (handler_pc <= end_pc): {start_pc=" + startPc
						+ ", end_pc=" + endPc + ", handler_pc=" + handlerPc + ", exception_type=" + exceptionType + "}");
			}
			
			// Add the try/catch data to the list.
			exceptionHandlers.add(new ExceptionHandler(startPcIndex, endPcIndex, handlerPcIndex, exceptionType));
		}
		
		// Return the result.
		return exceptionHandlers;
	}
	
	/**
	 * Gets the instruction index (target) for a branch instruction.
	 * @param byteCodeEntries - The byte code entries of the method.
	 * @param branchCodeEntry
	 * @return The instruction index or -1 if no matching byte code entry was found.
	 */
	private int getInstrIndexForBranch(ArrayList<ByteCodeEntry> byteCodeEntries, ByteCodeEntry branchCodeEntry) {
		int branchOffset = branchCodeEntry.getSignedInstructionArgs()[0];
		int absoluteIndex = branchCodeEntry.getOffset() + branchOffset;
		
		for(int i = 0; i < byteCodeEntries.size(); i++) {
			if(byteCodeEntries.get(i).getOffset() == absoluteIndex) {
				return byteCodeEntries.get(i).getInstructionIndex(); // Should equal "Return i;".
			}
		}
		return -1;
	}
	
}
