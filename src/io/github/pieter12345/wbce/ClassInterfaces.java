package io.github.pieter12345.wbce;

import java.io.IOException;
import java.util.ArrayList;

public class ClassInterfaces {
	
	private ArrayList<ClassInterface> interfaces;
	
	private ClassInterfaces(ArrayList<ClassInterface> interfaces) {
		this.interfaces = interfaces;
	}
	
	public static ClassInterfaces fromInputStream(FancyInputStream inStream) throws IOException {
		int interfaceCount = inStream.readTwoByteInt();
		ArrayList<ClassInterface> interfaces = new ArrayList<ClassInterface>(interfaceCount);
		for(int i = 0; i < interfaceCount; i++) {
			interfaces.add(ClassInterface.fromInputStream(inStream));
		}
		return new ClassInterfaces(interfaces);
	}
	
	public byte[] toBytes() {
		@SuppressWarnings("resource")
		FancyByteArrayOutputStream outStream = new FancyByteArrayOutputStream();
		outStream.writeTwoByteInteger(this.interfaces.size());
		for(int i = 0; i < this.interfaces.size(); i++) {
			outStream.write(this.interfaces.get(i).toBytes());
		}
		return outStream.toByteArray();
	}
	
	public int size() {
		return this.interfaces.size();
	}
	
	public ClassInterface getInterface(int index) {
		return this.interfaces.get(index);
	}
	
	public ClassInterface[] getInterfaces() {
		return this.interfaces.toArray(new ClassInterface[0]);
	}
	
	public void setInterface(int index, ClassInterface classInterface) {
		this.interfaces.set(index, classInterface);
	}
	
	public void addInterface(ClassInterface classInterface) {
		this.interfaces.add(classInterface);
	}
	
	public ClassInterface removeInterface(int index) {
		return this.interfaces.remove(index);
	}
	
	public boolean removeInterface(ClassInterface classInterface) {
		return this.interfaces.remove(classInterface);
	}
	
	public String getInterfaceName(int index, ClassConstantPool constPool) {
		if(constPool == null) {
			throw new NullPointerException("ClassConstantPool is null.");
		}
		int interfaceNameIndex = this.interfaces.get(index).getNameIndex();
		if(constPool.size() >= interfaceNameIndex) {
			return constPool.get(interfaceNameIndex).val(constPool);
		}
		return null;
	}
	
	public String toString(ClassConstantPool constPool) {
		String interfaceStr = "ClassInterfaces: {";
		for(int i = 0; i < interfaces.size(); i++) {
			int interfaceNameIndex = interfaces.get(i).getNameIndex();
			interfaceStr += i + ": " + interfaceNameIndex + "->" + (
					constPool != null && constPool.size() >= interfaceNameIndex ?
					constPool.get(interfaceNameIndex).val(constPool) : "~UNKNOWN VALUE") + " ";
			
		}
		return interfaceStr.trim() + "}";
	}
	
	public static class ClassInterface {
		private int interfaceNameIndex;
		
		public ClassInterface(int interfaceNameIndex) {
			this.interfaceNameIndex = interfaceNameIndex;
		}
		
		public static ClassInterface fromInputStream(FancyInputStream inStream) throws IOException {
			int interfaceNameIndex = inStream.readTwoByteInt();
			return new ClassInterface(interfaceNameIndex);
		}
		
		public byte[] toBytes() {
			@SuppressWarnings("resource")
			FancyByteArrayOutputStream outStream = new FancyByteArrayOutputStream();
			outStream.writeTwoByteInteger(this.interfaceNameIndex);
			return outStream.toByteArray();
		}
		
		public int getNameIndex() {
			return this.interfaceNameIndex;
		}
		
		public void setNameIndex(int interfaceNameIndex) {
			this.interfaceNameIndex = interfaceNameIndex;
		}
	}
	
}
