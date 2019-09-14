package io.github.pieter12345.wbce;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import io.github.pieter12345.wbce.ByteCodeInstruction.ByteCodeInstructionPayload;
import io.github.pieter12345.wbce.ClassMethods.ClassMethod;
import io.github.pieter12345.wbce.attribute.CodeAttribute;
import io.github.pieter12345.wbce.attribute.IAttribute;
import io.github.pieter12345.wbce.constantpool.ConstantPoolObject;
import io.github.pieter12345.wbce.constantpool.ConstantPoolString;
import io.github.pieter12345.wbce.decompile.DecompileException;
import io.github.pieter12345.wbce.decompile.Decompiler;
import io.github.pieter12345.wbce.utils.Utils;

public class ByteCodeReader {
	
	// Variables & Constants.
	private final File classFile;
	
	// Main method.
	public static void main(String[] args) {
		
//		String classFileName = "ByteCodeTest.class";
//		String classFileName = "C:/Users/Pietje/Documents/Java Eclipse Workspace/HelloWorld/bin/SimpleClassForByteCodeAnalysis.class";
		String classFileName = "C:/Users/Pietje/Documents/Java Eclipse Workspace/HelloWorld/bin/StructuresForByteCodeAnalysis.class";
		ClassFile classFile;
		try {
			classFile = ClassFile.readFromFile(new File(classFileName));
//			System.out.println("[DEBUG] [ByteCodeReader] readFile() executed.");
		} catch (Exception e) {
			System.out.println("[DEBUG] [ByteCodeReader] Exception thrown in readFile(). Here's the stacktrace:\n"
					+ Utils.getStacktrace(e));
			return;
		}
		
		System.out.println("Access flags: " + classFile.getAccessFlags().toString());
		System.exit(0);
		
		classFileName = "appletviewer.class";
		try {
			classFile = ClassFile.readFromFile(new File(classFileName));
//			System.out.println("[DEBUG] [ByteCodeReader] readFile() executed.");
		} catch (Exception e) {
			System.out.println("[DEBUG] [ByteCodeReader] Exception thrown in readFile(). Here's the stacktrace:\n"
					+ Utils.getStacktrace(e));
			return;
		}
		
		
//		// Print all attributes of the class.
//		String attrStr = classFile.getAttributes().toString(classFile.getConstantPool());
//		System.out.println(attrStr);
		
//		// Print all attributes of the methods.
//		ClassMethods methods = classFile.getMethods();
//		for(int i = 0; i < methods.size(); i++) {
//			String methodStr = methods.getMethod(i).toString(classFile.getConstantPool());
//			System.out.println(methodStr + ":\n\t"
//					+ methods.getMethod(i).getAttributes().toString(classFile.getConstantPool()).replaceAll("\n", "\t\n"));
//		}
		
		File decompileOutputFile = new File(classFileName.replaceAll("\\.class$", "") + "-decompile.java");
		Decompiler decomp = new Decompiler(classFile);
		
		try {
			decomp.decompile(decompileOutputFile);
		} catch (DecompileException | IOException e) {
			System.out.println("[DEBUG] [ByteCodeReader] Exception thrown while decompiling. Here's the stacktrace:\n"
					+ Utils.getStacktrace(e));
			return;
		}
		
//		System.out.println(classFile.toString().substring(0, 84500));
//		System.exit(0);
		
//		// Print the opcode of a method.
//		ClassConstantPool constPool = classFile.getConstantPool();
//		for(ClassMethod method : classFile.getMethods().getMethods()) {
//			String methodAccessFlags = method.getAccessFlagString();
//			String methodName = constPool.get(method.getNameIndex()).val(constPool);
//			String methodDesc = constPool.get(method.getDescIndex()).val(constPool);
//			String codeStr = "[DEBUG] Method code for " + methodAccessFlags + " " + methodName + methodDesc + ": {";
//			for(IAttribute attr : method.getAttributes().getAttributes()) {
//				if(attr instanceof CodeAttribute) {
//					byte[] opCodeBytes = ((CodeAttribute) attr).getCodeBytes();
//					for(int i = 0; i < opCodeBytes.length; i++) {
//						ByteCodeInstruction instr = ByteCodeInstruction.forOpCode(opCodeBytes[i]);
//						codeStr += "\n\t" + instr.getInstructionName();
//						for(ByteCodeInstructionPayload payload : instr.getPayloadTypes()) {
//							int val = 0;
//							for(int j = payload.getByteSize() - 1; j >= 0; j--) {
//								i++;
//								val |= ((opCodeBytes[i] & 0xFF) << (j * 8));
//							}
////							codeStr += " " + val;
//							if(payload.isConstPoolIndex()) {
////								codeStr += "->" + constPool.get(val).val(constPool);
//								if(val < 1 || val > constPool.size()) {
//									System.out.println(codeStr + "\n" + "~ERROR: Invalid constPool index found: " + val
//											+ " at index: " + i + ". Throwing exception.");
//									throw new RuntimeException("Invalid constPool index found: " + val);
////									codeStr += " INVALID_CONSTPOOL_INDEX: " + val;
//								} else {
//									codeStr += " " + constPool.get(val).simpleVal(constPool);
//								}
//							} else {
//								codeStr += " " + val;
//							}
//						}
//						if(instr.getPayload() == -1) {
//							System.out.println("[WARNING] [" + ByteCodeReader.class.getSimpleName() + "] OpCode instruction found without known payload: "
//									+ instr.getInstructionName() + ". Exceptions or misinterpretations might happen after this point.");
//						}
//					}
//				}
//			}
//			codeStr += "\n}";
//			System.out.println(codeStr);
//		}
		
		
// Code below stores and compares the class file.
//		// Store the class to "<name>-edited.class".
//		String targetFileName = classFileName;
//		if(targetFileName.length() > 6 && targetFileName.substring(targetFileName.length() - 6, targetFileName.length()).equals(".class")) {
//			targetFileName = targetFileName.substring(0, targetFileName.length() - 6);
//		}
//		File targetFile = new File(targetFileName + "-edited.class");
//		try {
////			if(targetFile.exists()) {
////				targetFile.delete();
////			}
//			classFile.writeToFile(targetFile, true);
//		} catch (IOException e) {
//			System.out.println("[DEBUG] [ByteCodeReader] Exception thrown in writeToFile(). Printing stacktrace:");
//			e.printStackTrace();
//			return;
//		}
//		
//		// Compare files to be able to debug differences.
//		File originalFile = new File(classFileName);
//		try {
//			InputStream inStream1 = new FileInputStream(originalFile);
//			InputStream inStream2 = new FileInputStream(targetFile);
//			
//			// Allocate a byte array for each class file bytes.
//			int fileSize1 = (int) originalFile.length();
//			byte[] bytes1 = new byte[fileSize1];
//			int fileSize2 = (int) targetFile.length();
//			byte[] bytes2 = new byte[fileSize2];
//			
//			// Read the class files.
//			int amount1 = inStream1.read(bytes1, 0, fileSize1);
//			inStream1.close();
//			int amount2 = inStream2.read(bytes2, 0, fileSize2);
//			inStream2.close();
//			if(amount1 != fileSize1) {
//				System.out.println("Failed to read (some) bytes of the class file 1 while comparing.");
//			}
//			if(amount2 != fileSize2) {
//				System.out.println("Failed to read (some) bytes of the class file 2 while comparing.");
//			}
//			if(fileSize1 != fileSize2) {
//				System.out.println("File size of original file does not match file size of stored file."
//						+ "\n\tOriginal: " + fileSize1 + "\tStored: " + fileSize2);
//			}
//			
//			// Compare file content.
//			boolean filesEqual = (bytes1.length == bytes2.length);
//			for(int i = 0; i < Math.min(bytes1.length, bytes2.length); i++) {
//				if(bytes1[i] != bytes2[i]) {
//					System.out.println("File content difference found at byte index: " + i);
//					filesEqual = false;
//					break;
//				}
//			}
//			if(filesEqual) {
//				System.out.println("Written file has the same content as the read file.");
//			}
//			
//		} catch(IOException e) {
//			e.printStackTrace();
//		}
		
		
	}
	
	public ByteCodeReader(File classFile) {
		this.classFile = classFile;
	}
	
	public void readByteCode() throws Exception {
		InputStream inStream = new FileInputStream(this.classFile);
		
		// Get the magic value (CAFEBABE, 4 bytes (8 bit integers)) to verify that it's a class file.
		byte[] magicValue = new byte[4];
		int amount = inStream.read(magicValue);
//		System.out.println("CAFEBABE bytes: " + magicValue[0] + "/" + magicValue[1] + "/" + magicValue[2] + "/" + magicValue[3]);
//		System.out.println("CAFEBABE expect: " + 0xCA + "/" + 0xFE + "/" + 0xBA + "/" + 0xBE);
		if(amount != 4 || magicValue[0] != 0xCA - 256 || magicValue[1] != 0xFE - 256
				|| magicValue[2] != 0xBA - 256 || magicValue[3] != 0xBE - 256) { // -256's to convert to signed bytes.
			inStream.close();
			throw new Exception("File is not a class file.");
		}
		
		// Allocate a byte array for the class file bytes.
		long fileSizeLong = this.classFile.length();
		if(fileSizeLong > 100e6) {
			inStream.close();
			throw new Exception("Class file too big (>100MB file size not supported).");
		}
		int fileSize = (int) fileSizeLong;
		byte[] bytes = new byte[fileSize];
		
		// Push the already read CAFEBABE bytes to the buffer.
		System.arraycopy(magicValue, 0, bytes, 0, 4);
		
		// Read the class file.
		amount = inStream.read(bytes, 4, fileSize - 4);
		inStream.close();
		if(amount != fileSize - 4) {
			throw new Exception("Failed to read (some) bytes of the class file.");
		}
		
		// Keep a bytes index.
		int bytesIndex = 4; // Skip CAFEBABE.
		
		// Get the minor version (2 bytes, big edian).
		int minorVersion = (bytes[bytesIndex++] << 8) + bytes[bytesIndex++];
		System.out.println("Minor version: " + minorVersion);
		
		// Get the major version (2 bytes, big edian).
		int majorVersion = (bytes[bytesIndex++] << 8) + bytes[bytesIndex++];
		System.out.println("Major version: " + majorVersion);
		
		// Get the constant pool count (2 bytes, big edian).
		int constPoolCount = (bytes[bytesIndex++] << 8) + bytes[bytesIndex++];
		System.out.println("Constant pool count: " + constPoolCount);
		
		// Get the constant pool values.
		for(int i = 1; i < constPoolCount; i++) { // Index 0 doesn't exist.
			byte tagByte = bytes[bytesIndex++];
			switch(tagByte) {
			case 1: { // Tag String.
				int strSize = (bytes[bytesIndex++] << 8) + bytes[bytesIndex++];
				byte[] strBytes = new byte[strSize];
				System.arraycopy(bytes, bytesIndex, strBytes, 0, strSize);
				bytesIndex += strSize;
				String strVal = new String(strBytes, StandardCharsets.UTF_8); // This is a modified UTF-8 format, this might not matter.
				System.out.println("\t" + i + " String: " + strVal);
				break;
			}
			case 3: { // Tag Integer.
				int intVal = (bytes[bytesIndex++] << 24) + (bytes[bytesIndex++] << 16) + (bytes[bytesIndex++] << 8) + bytes[bytesIndex++];
				System.out.println("\t" + i + " Integer: " + intVal);
				break;
			}
			case 4: { // Tag Float.
				int floatBits = (bytes[bytesIndex++] << 24) + (bytes[bytesIndex++] << 16) + (bytes[bytesIndex++] << 8) + bytes[bytesIndex++];
				float floatVal = Float.intBitsToFloat(floatBits);
				System.out.println("\t" + i + " Float: " + floatVal);
				break;
			}
			case 5: { // Tag Long.
				long longVal = (bytes[bytesIndex++] << 56) + (bytes[bytesIndex++] << 48) + (bytes[bytesIndex++] << 40) + (bytes[bytesIndex++] << 32)
						+ (bytes[bytesIndex++] << 24) + (bytes[bytesIndex++] << 16) + (bytes[bytesIndex++] << 8) + bytes[bytesIndex++];
				System.out.println("\t" + i + " Long: " + longVal);
				break;
			}
			case 6: { // Tag Double.
				long longBits = (bytes[bytesIndex++] << 56) + (bytes[bytesIndex++] << 48) + (bytes[bytesIndex++] << 40) + (bytes[bytesIndex++] << 32)
						+ (bytes[bytesIndex++] << 24) + (bytes[bytesIndex++] << 16) + (bytes[bytesIndex++] << 8) + bytes[bytesIndex++];
				
				Double doubleVal = Double.longBitsToDouble(longBits);
				System.out.println("\t" + i + " Double: " + doubleVal);
				break;
			}
			case 7: { // Tag Class reference (index of a String in the constant pool).
				int index = (bytes[bytesIndex++] << 8) + bytes[bytesIndex++];
				System.out.println("\t" + i + " Class reference: Index " + index);
				break;
			}
			case 8: { // Tag String reference (index of a String in the constant pool).
				int index = (bytes[bytesIndex++] << 8) + bytes[bytesIndex++];
				System.out.println("\t" + i + " String reference: Index " + index);
				break;
			}
			case 9: { // Tag Field reference (index of a Tag Class entry and index of a Name and Type descriptor).
				int classIndex = (bytes[bytesIndex++] << 8) + bytes[bytesIndex++];
				int nameAndTypeDescIndex = (bytes[bytesIndex++] << 8) + bytes[bytesIndex++];
				System.out.println("\t" + i + " Field reference: Class index: " + classIndex
						+ ", Name & Type descriptor index: " + nameAndTypeDescIndex);
				break;
			}
			case 10: { // Tag Method reference (index of a Tag Class entry and index of a Name and Type descriptor).
				int classIndex = (bytes[bytesIndex++] << 8) + bytes[bytesIndex++];
				int nameAndTypeDescIndex = (bytes[bytesIndex++] << 8) + bytes[bytesIndex++];
				System.out.println("\t" + i + " Method reference: Class index: " + classIndex
						+ ", Name & Type descriptor index: " + nameAndTypeDescIndex);
				break;
			}
			case 11: { // Tag Interface method reference (index of a Tag Class entry and index of a Name and Type descriptor).
				int classIndex = (bytes[bytesIndex++] << 8) + bytes[bytesIndex++];
				int nameAndTypeDescIndex = (bytes[bytesIndex++] << 8) + bytes[bytesIndex++];
				System.out.println("\t" + i + " Interface method reference: Class index: " + classIndex
						+ ", Name & Type descriptor index: " + nameAndTypeDescIndex);
				break;
			}
			case 12: { // Tag Name and Type descriptor (index of a String (name/identifier) and index of a String (specially encoded type descriptor)).
				int nameIndex = (bytes[bytesIndex++] << 8) + bytes[bytesIndex++];
				int typeDescIndex = (bytes[bytesIndex++] << 8) + bytes[bytesIndex++];
				System.out.println("\t" + i + " Name & Type descriptor: Identifier(name) index: " + nameIndex
						+ ", Type descriptor index: " + typeDescIndex);
				break;
			}
			case 15: { // Tag Method handle (1 byte type + 2 bytes index of method in constant pool).
				byte typeByte = bytes[bytesIndex++];
				int methodIndex = (bytes[bytesIndex++] << 8) + bytes[bytesIndex++];
				System.out.println("\t" + i + " Method handle: Type: " + typeByte
						+ ", Method index: " + methodIndex);
				break;
			}
			case 16: { // Tag Method type (2 byte index in constant pool).
				int typeIndex = (bytes[bytesIndex++] << 8) + bytes[bytesIndex++];
				System.out.println("\t" + i + " Method type (index): " + typeIndex);
				break;
			}
			case 18: { // Tag InvokeDynamic (4 bytes). Format: u2 bootstrap_method_attr_index, u2 name_and_type_index.
				System.out.println("\t" + i + " InvokeDynamic (raw): "
						+ bytes[bytesIndex++] + ", " + bytes[bytesIndex++] + ", " + bytes[bytesIndex++] + ", " + bytes[bytesIndex++]);
				break;
			}
			}
		}
		
		// Read the access flags (2 bytes bitmask).
		{
			int accessFlags = (bytes[bytesIndex++] << 8) + bytes[bytesIndex++];
			boolean classPublic     = (accessFlags & 0x0001) == 0x0001;
			boolean classFinal      = (accessFlags & 0x0010) == 0x0010;
			boolean classSuper      = (accessFlags & 0x0020) == 0x0020;
			boolean classInterface  = (accessFlags & 0x0200) == 0x0200;
			boolean classAbstract   = (accessFlags & 0x0400) == 0x0400;
			boolean classSynthetic  = (accessFlags & 0x1000) == 0x1000;
			boolean classAnnotation = (accessFlags & 0x2000) == 0x2000;
			boolean classEnum       = (accessFlags & 0x4000) == 0x4000;
			System.out.println("Access flags: " + accessFlags + " -> "
					+ (classPublic ? "public " : "") + (classFinal ? "final " : "")
					+ (classSuper ? "super " : "") + (classInterface ? "interface " : "")
					+ (classAbstract ? "abstract " : "") + (classSynthetic ? "synthetic " : "")
					+ (classAnnotation ? "annotation " : "") + (classEnum ? "enum " : "")
				);
		}
		
		// Get class identifier (index in constant pool).
		int classIdentifierIndex = (bytes[bytesIndex++] << 8) + bytes[bytesIndex++];
		System.out.println("Class identifier index: " + classIdentifierIndex);
		
		// Get super class identifier (index in constant pool).
		int superClassIdentifierIndex = (bytes[bytesIndex++] << 8) + bytes[bytesIndex++];
		System.out.println("Super class identifier index: " + superClassIdentifierIndex);
		
		// Get the interfaces.
		int interfaceCount = (bytes[bytesIndex++] << 8) + bytes[bytesIndex++];
		System.out.println("Interface count: " + interfaceCount);
		for(int i = 0; i < interfaceCount; i++) {
			System.out.println("\tInterface (index): " + (bytes[bytesIndex++] << 8) + bytes[bytesIndex++]);
		}
		
		// Get the fields.
		int fieldCount = (bytes[bytesIndex++] << 8) + bytes[bytesIndex++];
		System.out.println("Field count: " + fieldCount);
		for(int i = 0; i < fieldCount; i++) {
			int accessFlags = (bytes[bytesIndex++] << 8) + bytes[bytesIndex++];
			boolean fieldPublic     = (accessFlags & 0x0001) == 0x0001;
			boolean fieldPrivate    = (accessFlags & 0x0002) == 0x0002;
			boolean fieldProtected  = (accessFlags & 0x0004) == 0x0004;
			boolean fieldStatic     = (accessFlags & 0x0008) == 0x0008;
			boolean fieldFinal      = (accessFlags & 0x0010) == 0x0010;
			boolean fieldVolatile   = (accessFlags & 0x0040) == 0x0040;
			boolean fieldTransient  = (accessFlags & 0x0080) == 0x0080;
			boolean fieldSynthetic  = (accessFlags & 0x1000) == 0x1000;
			boolean fieldEnum       = (accessFlags & 0x4000) == 0x4000;

			int fieldNameIndex = (bytes[bytesIndex++] << 8) + bytes[bytesIndex++];
			int fieldDescIndex = (bytes[bytesIndex++] << 8) + bytes[bytesIndex++];
			int fieldAttrCount = (bytes[bytesIndex++] << 8) + bytes[bytesIndex++];
			
			String fieldAttrStr = "";
			for(int j = 0; j < fieldAttrCount; j++) {
				int attrNameIndex = (bytes[bytesIndex++] << 8) + bytes[bytesIndex++];
				int attrInfoLength = (bytes[bytesIndex++] << 24) + (bytes[bytesIndex++] << 16) + (bytes[bytesIndex++] << 8) + bytes[bytesIndex++];
				byte[] attrInfoBytes = new byte[attrInfoLength];
				System.arraycopy(bytes, bytesIndex, attrInfoBytes, 0, attrInfoLength);
				bytesIndex += attrInfoLength;
//				String attrInfoStr = new String(attrInfoBytes, StandardCharsets.UTF_8);
				fieldAttrStr += "\n\t\t\tAttribute: Name (index): " + attrNameIndex + ", Value: --"; // + attrInfoStr;
			}
			
			System.out.println("\tField name (index): " + fieldNameIndex + ":"
					+ "\n\t\tAccess flags: " + accessFlags + " -> "
					+ (fieldPublic ? "public " : "") + (fieldPrivate ? "private " : "")
					+ (fieldProtected ? "protected " : "") + (fieldStatic ? "static " : "")
					+ (fieldFinal ? "final " : "") + (fieldVolatile ? "volatile " : "")
					+ (fieldTransient ? "transient " : "") + (fieldSynthetic ? "synthetic " : "")
					+ (fieldEnum ? "enum " : "")
					+ "\n\t\tDescriptor (index): " + fieldDescIndex
					+ "\n\t\tAttribute count: " + fieldAttrCount + fieldAttrStr
				);
		}
		
		// Get the methods.
		int methodCount = (bytes[bytesIndex++] << 8) + bytes[bytesIndex++];
		System.out.println("Method count: " + methodCount);
		for(int i = 0; i < methodCount; i++) {
			int accessFlags = (bytes[bytesIndex++] << 8) + bytes[bytesIndex++];
			boolean methodPublic       = (accessFlags & 0x0001) == 0x0001;
			boolean methodPrivate      = (accessFlags & 0x0002) == 0x0002;
			boolean methodProtected    = (accessFlags & 0x0004) == 0x0004;
			boolean methodStatic       = (accessFlags & 0x0008) == 0x0008;
			boolean methodFinal        = (accessFlags & 0x0010) == 0x0010;
			boolean methodSynchronized = (accessFlags & 0x0020) == 0x0020;
			boolean methodBridge       = (accessFlags & 0x0040) == 0x0040;
			boolean methodVarargs      = (accessFlags & 0x0080) == 0x0080;
			boolean methodNative       = (accessFlags & 0x0100) == 0x0100;
			boolean methodAbstract     = (accessFlags & 0x0400) == 0x0400;
			boolean methodStrict       = (accessFlags & 0x0800) == 0x0800;
			boolean methodSynthetic    = (accessFlags & 0x1000) == 0x1000;

			int methodNameIndex = (bytes[bytesIndex++] << 8) + bytes[bytesIndex++];
			int methodDescIndex = (bytes[bytesIndex++] << 8) + bytes[bytesIndex++];
			int methodAttrCount = (bytes[bytesIndex++] << 8) + bytes[bytesIndex++];
			
			String methodAttrStr = "";
			for(int j = 0; j < methodAttrCount; j++) {
				int attrNameIndex = (bytes[bytesIndex++] << 8) + bytes[bytesIndex++];
				int attrInfoLength = (bytes[bytesIndex++] << 24) + (bytes[bytesIndex++] << 16) + (bytes[bytesIndex++] << 8) + bytes[bytesIndex++];
				byte[] attrInfoBytes = new byte[attrInfoLength];
				System.arraycopy(bytes, bytesIndex, attrInfoBytes, 0, attrInfoLength);
//				for(int k = 0; k < attrInfoBytes.length; k++) {
//					System.out.print(attrInfoBytes[k] + ", ");
//				}
//				System.out.println();
				bytesIndex += attrInfoLength;
//				String attrInfoStr = new String(attrInfoBytes, StandardCharsets.UTF_8);
				methodAttrStr += "\n\t\t\tAttribute: Name (index): " + attrNameIndex + ", Value: --"; // + attrInfoStr;
			}
			
			System.out.println("\tMethod name (index): " + methodNameIndex + ":"
					+ "\n\t\tAccess flags: " + accessFlags + " -> "
					+ (methodPublic ? "public " : "") + (methodPrivate ? "private " : "")
					+ (methodProtected ? "protected " : "") + (methodStatic ? "static " : "")
					+ (methodFinal ? "final " : "") + (methodSynchronized ? "synchronized " : "")
					+ (methodBridge ? "bridge " : "") + (methodVarargs ? "varargs " : "")
					+ (methodNative ? "native " : "") + (methodAbstract ? "abstract " : "")
					+ (methodStrict ? "strict " : "") + (methodSynthetic ? "synthetic " : "")
					+ "\n\t\tDescriptor (index): " + methodDescIndex
					+ "\n\t\tAttribute count: " + methodAttrCount + methodAttrStr
				);
		}
		
		
		// Get the attributes.
		int attrCount = (bytes[bytesIndex++] << 8) + bytes[bytesIndex++];
		String attrStr = "";
		for(int j = 0; j < attrCount; j++) {
			int attrNameIndex = (bytes[bytesIndex++] << 8) + bytes[bytesIndex++];
			int attrInfoLength = (bytes[bytesIndex++] << 24) + (bytes[bytesIndex++] << 16) + (bytes[bytesIndex++] << 8) + bytes[bytesIndex++];
			byte[] attrInfoBytes = new byte[attrInfoLength];
			System.arraycopy(bytes, bytesIndex, attrInfoBytes, 0, attrInfoLength);
//			for(int k = 0; k < attrInfoBytes.length; k++) {
//				System.out.print(attrInfoBytes[k] + ", ");
//			}
//			System.out.println();
			bytesIndex += attrInfoLength;
//			String attrInfoStr = new String(attrInfoBytes, StandardCharsets.UTF_8);
			attrStr += "\n\t\t\tAttribute: Name (index): " + attrNameIndex + ", Value: --"; // + attrInfoStr;
		}
		System.out.println("Attribute count: " + attrCount + attrStr);
		
	}
	
	/**
	 * Replaces JOOQ's startup screen in DefaultRenderContext.class within the MyWarp Bukkit plugin.
	 * Class is in package: io\github\mywarp\mywarp\internal\jooq\impl.
	 */
	public static void replaceDefaultRenderContextStartupScreen() {
		String classFileName = "DefaultRenderContext.class";
		ClassFile classFile;
		try {
			classFile = ClassFile.readFromFile(new File(classFileName));
		} catch (Exception e) {
			System.out.println("[DEBUG] [ByteCodeReader] Exception thrown in readFile(). Printing stacktrace:");
			e.printStackTrace();
			return;
		}
		
		
		ClassConstantPool constPool = classFile.getConstantPool();
		String search = "\n                                      " +
                "\n@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@" +
                "\n@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@" +
                "\n@@@@@@@@@@@@@@@@  @@        @@@@@@@@@@" +
                "\n@@@@@@@@@@@@@@@@@@@@        @@@@@@@@@@" +
                "\n@@@@@@@@@@@@@@@@  @@  @@    @@@@@@@@@@" +
                "\n@@@@@@@@@@  @@@@  @@  @@    @@@@@@@@@@" +
                "\n@@@@@@@@@@        @@        @@@@@@@@@@" +
                "\n@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@" +
                "\n@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@" +
                "\n@@@@@@@@@@        @@        @@@@@@@@@@" +
                "\n@@@@@@@@@@    @@  @@  @@@@  @@@@@@@@@@" +
                "\n@@@@@@@@@@    @@  @@  @@@@  @@@@@@@@@@" +
                "\n@@@@@@@@@@        @@  @  @  @@@@@@@@@@" +
                "\n@@@@@@@@@@        @@        @@@@@@@@@@" +
                "\n@@@@@@@@@@@@@@@@@@@@@@@  @@@@@@@@@@@@@" +
                "\n@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@" +
                "\n@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@  ";
		String search2 = "\n                                      ";
		for(int i = 1; i <= constPool.size(); i++) {
			ConstantPoolObject obj = constPool.get(i);
			if(obj instanceof ConstantPoolString && (((ConstantPoolString) obj).getString().equals(search)
					|| ((ConstantPoolString) obj).getString().equals(search2))) {
				((ConstantPoolString) obj).setString("");
				System.out.println("JOOQ startup screen string replaced.");
			}
		}
		try {
			classFile.writeToFile(new File(
					classFileName.replaceAll("\\.class$", "") + "-no_startup_screen.class"), true);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
}
