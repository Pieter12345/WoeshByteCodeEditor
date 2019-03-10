package io.github.pieter12345.wbce.decompile;

import java.util.HashMap;

public class LocalVariableGen {
	
	// Variables & Constants.
	private final HashMap<Integer, LocalVariable> localVarMap = new HashMap<Integer, LocalVariable>();
	
	public LocalVariableGen() {
	}
	
	/**
	 * getVar method.
	 * Gets the requested variable or creates a new one if it did not yet exist.
	 * @param localVariableIndex - The local variable index.
	 * @param assignType - The type of the variable. This is ignored if the variable already exists.
	 * @return The requested LocalVariable.
	 */
	public LocalVariable getVar(int localVariableIndex, String assignType) {
		LocalVariable localVar = this.localVarMap.get(localVariableIndex);
		if(localVar == null) {
			if(assignType == null) {
				throw new NullPointerException("Variable assign type cannot be null if the variable does not yet exist.");
			}
			localVar = new LocalVariable("var" + localVariableIndex, assignType);
			this.localVarMap.put(localVariableIndex, localVar);
		}
		return localVar;
	}
	
	/**
	 * getVar method.
	 * Gets the requested variable.
	 * @param localVariableIndex - The local variable index.
	 * @return The requested LocalVariable.
	 * @throws IndexOutOfBoundsException If no variable exists at the localVariableIndex.
	 */
	public LocalVariable getVar(int localVariableIndex) {
		LocalVariable localVar = this.localVarMap.get(localVariableIndex);
		if(localVar == null) {
			throw new IndexOutOfBoundsException("Local variable at index " + localVariableIndex + " does not exist.");
		}
		return localVar;
	}
	
	/**
	 * setVar method.
	 * Sets the given LocalVariable to the given local variable index.
	 * @param localVariableIndex - The local variable index.
	 * @param localVar - The LocalVariable to set.
	 */
	public void setVar(int localVariableIndex, LocalVariable localVar) {
		this.localVarMap.put(localVariableIndex, localVar);
	}
	
	/**
	 * containsVar method.
	 * @param localVariableIndex - The local variable index.
	 * @return True if the variable exists, false otherwise.
	 */
	public boolean containsVar(int localVariableIndex) {
		return this.localVarMap.containsKey(localVariableIndex);
	}
	
	/**
	 * @deprecated Use getVar(int, String).getName() instead.
	 */
	public String getVarName(int localVariableIndex) {
		return this.getVar(localVariableIndex, null).getName();
	}
	
	/**
	 * @deprecated Use getVar(int, String).setName(String) instead.
	 */
	public void setVarName(int localVariableIndex, String name) {
		this.getVar(localVariableIndex, null).setName(name);
	}
	
	public static class LocalVariable {
		
		// Variables & Constants.
		private String varName;
		private String returnType;
		
		public LocalVariable(String varName, String returnType) {
			this.varName = varName;
			this.returnType = returnType;
		}
		
		public String getName() {
			return this.varName;
		}
		
		public void setName(String name) {
			this.varName = name;
		}
		
		public String getReturnType() {
			return this.returnType;
		}
		
		public void setReturnType(String returnType) {
			if(returnType == null) {
				throw new NullPointerException("Return type may not be null.");
			}
			this.returnType = returnType;
		}
		
	}
}
