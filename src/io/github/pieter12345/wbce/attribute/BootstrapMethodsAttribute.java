package io.github.pieter12345.wbce.attribute;

import java.io.IOException;
import java.util.ArrayList;

import io.github.pieter12345.wbce.ClassConstantPool;
import io.github.pieter12345.wbce.FancyByteArrayOutputStream;
import io.github.pieter12345.wbce.FancyInputStream;

public class BootstrapMethodsAttribute implements IAttribute {
	
	// Variables & Constants.
	private int attrNameIndex; // Index of the "BootstrapMethods" string in the constant pool.
	private ArrayList<BootstrapMethod> bootstrapMethods;
	
	public BootstrapMethodsAttribute(int attrNameIndex, ArrayList<BootstrapMethod> bootstrapMethods) {
		this.attrNameIndex = attrNameIndex;
		this.bootstrapMethods = bootstrapMethods;
	}
	
	public static BootstrapMethodsAttribute fromInputStream(int attrNameIndex, FancyInputStream inStream) throws IOException {
		inStream.readFourByteInt(); // attrInfoLength.
		int bootstrapMethodCount = inStream.readTwoByteInt();
		ArrayList<BootstrapMethod> bootstrapMethods = new ArrayList<BootstrapMethod>(bootstrapMethodCount);
		for(int i = 0; i < bootstrapMethodCount; i++) {
			bootstrapMethods.add(BootstrapMethod.fromInputStream(inStream));
		}
		return new BootstrapMethodsAttribute(attrNameIndex, bootstrapMethods);
	}
	
	@Override
	public byte[] toBytes() {
		@SuppressWarnings("resource")
		FancyByteArrayOutputStream outStream = new FancyByteArrayOutputStream();
		outStream.writeTwoByteInteger(this.bootstrapMethods.size());
		for(int i = 0; i < this.bootstrapMethods.size(); i++) {
			outStream.write(this.bootstrapMethods.get(i).toBytes());
		}
		byte[] outStreamBytes = outStream.toByteArray();
		
		@SuppressWarnings("resource")
		FancyByteArrayOutputStream outStream2 = new FancyByteArrayOutputStream();
		outStream2.writeTwoByteInteger(this.attrNameIndex);
		outStream2.writeFourByteInteger(outStreamBytes.length);
		outStream2.write(outStreamBytes);
		
		return outStream2.toByteArray();
	}
	
	public BootstrapMethod getBootstrapMethod(int index) {
		return this.bootstrapMethods.get(index);
	}
	
	public void setBootstrapMethod(int index, BootstrapMethod bootstrapMethod) {
		this.bootstrapMethods.set(index, bootstrapMethod);
	}
	
	public void addBootstrapMethod(BootstrapMethod bootstrapMethod) {
		this.bootstrapMethods.add(bootstrapMethod);
	}
	
	public BootstrapMethod removeBootstrapMetho(int index) {
		return this.bootstrapMethods.remove(index);
	}
	
	public boolean removeBootstrapMetho(BootstrapMethod bootstrapMethod) {
		return this.bootstrapMethods.remove(bootstrapMethod);
	}
	
	@Override
	public String toString(ClassConstantPool constPool) {
		String bootstrapMethodsStr = "";
		for(int i = 0; i < this.bootstrapMethods.size(); i++) {
			bootstrapMethodsStr += i + ": " + this.bootstrapMethods.get(i).toString(constPool) + " ";
		}
		return BootstrapMethodsAttribute.class.getSimpleName() + ": {" + bootstrapMethodsStr.trim() + "}";
	}
	
	public static class BootstrapMethod {
		
		// Variables & Constants.
		private int methodRefIndex;
		private BootstrapArguments bootstrapArgs;
		public BootstrapMethod(int start_pc, BootstrapArguments bootstrapArgs) {
			this.methodRefIndex = start_pc;
			this.bootstrapArgs = bootstrapArgs;
		}
		
		public static BootstrapMethod fromInputStream(FancyInputStream inStream) throws IOException {
			int methodRefIndex = inStream.readTwoByteInt();
			BootstrapArguments bootstrapArgs = BootstrapArguments.fromInputStream(inStream);
			return new BootstrapMethod(methodRefIndex, bootstrapArgs);
		}
		
		public byte[] toBytes() {
			@SuppressWarnings("resource")
			FancyByteArrayOutputStream outStream = new FancyByteArrayOutputStream();
			outStream.writeTwoByteInteger(this.methodRefIndex);
			outStream.write(this.bootstrapArgs.toBytes());
			return outStream.toByteArray();
		}
		
		public int getMethodRefIndex() {
			return this.methodRefIndex;
		}
		
		public void setMethodRefIndex(int methodRefIndex) {
			this.methodRefIndex = methodRefIndex;
		}
		
		public BootstrapArguments getBootstrapArguments() {
			return this.bootstrapArgs;
		}
		
		public void setBootstrapArguments(BootstrapArguments bootstrapArgs) {
			this.bootstrapArgs = bootstrapArgs;
		}
		
		public String toString(ClassConstantPool constPool) {
			String methodRef = (constPool != null && constPool.size() >= this.methodRefIndex ?
					constPool.get(this.methodRefIndex).val(constPool) : "~UNKNOWN_METHOD_REF");
			return BootstrapMethod.class.getSimpleName() + ": {methodRefIndex: " + this.methodRefIndex
					+ "->" + methodRef + ", " + this.bootstrapArgs.toString(constPool) + "}";
		}
	}
	
	public static class BootstrapArguments {
		
		// Variables & Constants.
		private ArrayList<Integer> bootstrapArgIndices;
		
		public BootstrapArguments(ArrayList<Integer> bootstrapArgIndices) {
			this.bootstrapArgIndices = bootstrapArgIndices;
		}
		
		public static BootstrapArguments fromInputStream(FancyInputStream inStream) throws IOException {
			int bootstrapArgSize = inStream.readTwoByteInt();
			ArrayList<Integer> bootstrapArgIndices = new ArrayList<Integer>(bootstrapArgSize);
			for(int i = 0; i < bootstrapArgSize; i++) {
				bootstrapArgIndices.add(inStream.readTwoByteInt());
			}
			return new BootstrapArguments(bootstrapArgIndices);
		}
		
		public byte[] toBytes() {
			@SuppressWarnings("resource")
			FancyByteArrayOutputStream outStream = new FancyByteArrayOutputStream();
			outStream.writeTwoByteInteger(this.bootstrapArgIndices.size());
			for(int i = 0; i < this.bootstrapArgIndices.size(); i++) {
				outStream.writeTwoByteInteger(this.bootstrapArgIndices.get(i));
			}
			return outStream.toByteArray();
		}

		public String toString(ClassConstantPool constPool) {
			String bootstrapArgsStr = "";
			for(int i = 0; i < this.bootstrapArgIndices.size(); i++) {
				
			}
			for(int i = 0; i < this.bootstrapArgIndices.size(); i++) {
				int bootstrapArgIndex = this.bootstrapArgIndices.get(i);
				String bootstrapArg = (constPool != null && constPool.size() >= bootstrapArgIndex ?
						constPool.get(bootstrapArgIndex).val(constPool) : "~UNKNOWN_ARG");
				bootstrapArgsStr += i + ": " + bootstrapArgIndex + "->" + bootstrapArg;
			}
			return BootstrapArguments.class.getSimpleName() + ": {" + bootstrapArgsStr.trim() + "}";
		}
	}
}
