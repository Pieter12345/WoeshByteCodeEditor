package io.github.pieter12345.wbce.attribute;
//
//import java.io.IOException;
//import java.util.ArrayList;
//
//import io.github.pieter12345.wbce.FancyInputStream;
//import io.github.pieter12345.wbce.ClassConstantPool;
//import io.github.pieter12345.wbce.FancyByteArrayOutputStream;
//
//public class StackMapTableAttribute_UNUSED implements IAttribute {
//	
//	// Variables & Constants.
//	private int attrNameIndex; // Index of the "StackMapTable" string in the constant pool.
//	private ArrayList<StackMapFrame> frames;
//	
//	public StackMapTableAttribute_UNUSED(ArrayList<StackMapFrame> frames) {
//		this.frames = frames;
//	}
//	
//	public static StackMapTableAttribute fromInputStream(int attrNameIndex, FancyInputStream inStream) throws IOException {
//		int stackMapCount = inStream.readTwoByteInt();
//		ArrayList<StackMapFrame> frames = new ArrayList<StackMapFrame>(stackMapCount);
//		for(int i = 0; i < stackMapCount; i++) {
//			byte frameType = inStream.readByte();
//			
//			
//			
//		}
//		return new StackMapTableAttribute(frames);
//	}
//	
//	@Override
//	public byte[] toBytes() {
//		@SuppressWarnings("resource")
//		FancyByteArrayOutputStream outStream = new FancyByteArrayOutputStream();
//		outStream.writeTwoByteInteger(this.maxStack);
//		outStream.writeTwoByteInteger(this.maxLocals);
//		outStream.writeFourByteInteger(this.codeBytes.length);
//		outStream.write(this.codeBytes);
//		outStream.write(this.exceptionTable.toBytes());
//		outStream.write(this.attributes.toBytes());
//		byte[] outStreamBytes = outStream.toByteArray();
//		
//		@SuppressWarnings("resource")
//		FancyByteArrayOutputStream outStream2 = new FancyByteArrayOutputStream();
//		outStream2.writeTwoByteInteger(this.attrNameIndex);
//		outStream2.writeFourByteInteger(outStreamBytes.length);
//		outStream2.write(outStreamBytes);
//		
//		return outStream2.toByteArray();
//	}
//	
//	@Override
//	public String toString(ClassConstantPool constPool) {
//		return "CodeAttribute: {max_stack=" + this.maxStack + ", max_locals=" + this.maxLocals
//				+ ", code_bytes[" + this.codeBytes.length + "], " + this.exceptionTable.toString(constPool)
//				+ ", " + this.attributes.toString(constPool) + "}";
//	}
//	
//	public static class ExceptionTable {
//		
//		// Variables & Constants.
//		private ArrayList<ExceptionTableEntry> exceptionTableEntries;
//		
//		public ExceptionTable(ArrayList<ExceptionTableEntry> exceptionTableEntries) {
//			this.exceptionTableEntries = exceptionTableEntries;
//		}
//		
//		public static Object fromInputStream(FancyInputStream inStream) throws IOException {
//			// TODO
//			return new Object();
//		}
//		
//		public byte[] toBytes() {
//			@SuppressWarnings("resource")
//			FancyByteArrayOutputStream outStream = new FancyByteArrayOutputStream();
//			// TODO
//			return outStream.toByteArray();
//		}
//		
//		public String toString(ClassConstantPool constPool) {
//			// TODO
//			return null;
//		}
//		
//	}
//	
//}
