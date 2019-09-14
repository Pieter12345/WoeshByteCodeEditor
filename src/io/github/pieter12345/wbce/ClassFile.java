package io.github.pieter12345.wbce;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import io.github.pieter12345.wbce.ClassFields.ClassField;
import io.github.pieter12345.wbce.ClassMethods.ClassMethod;
import io.github.pieter12345.wbce.attribute.AttributeSet;
import io.github.pieter12345.wbce.constantpool.ConstantPoolObject;

/*
 * TODO - Revise this class:
 * Add a way to generate the constant pool from whatever uses it.
 *     This comes with allowing strings instead of constant pool indices to initialize some fields.
 *     Other things that are defined through indices in the constant pool should then also
 *       contain another representation.
 */
public class ClassFile {
	
	// Variables & Constants.
	private int minorVersion;
	private int majorVersion;
	private ClassConstantPool constPool;
	private ClassAccessFlags accessFlags;
	private int thisClassIdentifierIndex;
	private int superClassIdentifierIndex;
	private ClassInterfaces interfaces;
	private ClassFields fields;
	private ClassMethods methods;
	private AttributeSet attributes;
	
	public ClassFile(int minorVersion, int majorVersion, ClassConstantPool constPool, ClassAccessFlags accessFlags,
			int thisClassIdentifierIndex, int superClassIdentifierIndex,
			ClassInterfaces interfaces, ClassFields fields, ClassMethods methods, AttributeSet attributes) {
		this.minorVersion = minorVersion;
		this.majorVersion = majorVersion;
		this.constPool = constPool;
		this.accessFlags = accessFlags;
		this.thisClassIdentifierIndex = thisClassIdentifierIndex;
		this.superClassIdentifierIndex = superClassIdentifierIndex;
		this.interfaces = interfaces;
		this.fields = fields;
		this.methods = methods;
		this.attributes = attributes;
	}
	
	/**
	 * Reads the given class file and returns a {@link ClassFile} representing that file.
	 * @param classFile - The class file to read.
	 * @return A {@link ClassFile} representing the given class file.
	 * @throws FileNotFoundException If the given file was not found.
	 * @throws IOException If an I/O error occurs while reading the given file.
	 * @throws Exception If the file has an illegal or unsupported format.
	 */
	public static ClassFile readFromFile(File classFile) throws FileNotFoundException, IOException, Exception {
		FancyInputStream inStream = new FancyInputStream(new FileInputStream(classFile));
		
		// Get the magic value (CAFEBABE, 4 bytes as 8 bit integers) to verify that the file is a class file.
		int magicValue = inStream.readFourByteInt();
		if(magicValue != 0xCAFEBABE) {
			inStream.close();
			throw new Exception("File is not a class file.");
		}
		
		// Get the minor version (2 bytes, big edian).
		int minorVersion = inStream.readTwoByteInt();
		
		// Get the major version (2 bytes, big edian).
		int majorVersion = inStream.readTwoByteInt();
		
		// Get the constant pool.
		ClassConstantPool constPool = ClassConstantPool.fromInputStream(inStream);
		
		// Get the access flags (2 bytes bitmask).
		ClassAccessFlags accessFlags = new ClassAccessFlags(inStream.readTwoByteInt());
		
		// Get class identifier (index in constant pool).
		int thisClassIdentifierIndex = inStream.readTwoByteInt();
		
		// Get super class identifier (index in constant pool).
		int superClassIdentifierIndex = inStream.readTwoByteInt();
		
		// Get the interfaces.
		ClassInterfaces interfaces = ClassInterfaces.fromInputStream(inStream);

		// Get the fields.
		ClassFields fields = ClassFields.fromInputStream(inStream, constPool);
		
		// Get the methods.
		ClassMethods methods = ClassMethods.fromInputStream(inStream, constPool);
		
		// Get the class attributes.
		AttributeSet attributes = AttributeSet.fromInputStream(inStream, constPool);
		
		// Check if the end of the file has been reached.
		if(inStream.read() != -1) {
			System.out.println("[Warning] [" + ClassFile.class.getSimpleName() + "]"
					+ " The class file contains more bytes then expected.");
		}
		
		// Return a ClassFile representing the read data.
		return new ClassFile(minorVersion, majorVersion, constPool, accessFlags,
				thisClassIdentifierIndex, superClassIdentifierIndex, interfaces, fields, methods, attributes);
	}
	
	/**
	 * Writes this {@link ClassFile} to the given file.
	 * @param file - The file to write to.
	 * @param overwrite - If {@code true}, the given file is overwritten if it already exists.
	 * @throws IOException If an I/O error occurs during the writing or if the file already exists and the overwrite
	 * parameter is {@code false} or if the parent directory of the file does not yet exist.
	 */
	public void writeToFile(File file, boolean overwrite) throws IOException {
		if(file.exists() && !overwrite) {
			throw new IOException("File already exists: " + file.getAbsolutePath());
		}
		if(!file.getAbsoluteFile().getParentFile().exists()) {
			throw new IOException(
					"Parent directory does not exist: " + file.getAbsoluteFile().getParentFile().getAbsolutePath());
		}
		file.createNewFile();
		FileOutputStream outStream = new FileOutputStream(file);
		outStream.write(this.toBytes());
		outStream.close();
	}
	
	/**
	 * Gets this {@link ClassFile} as bytes in the class file format.
	 * @return A byte array containing this {@link ClassFile} in the class file format.
	 */
	public byte[] toBytes() {
		@SuppressWarnings("resource") // No resources leak from not closing the output stream.
		FancyByteArrayOutputStream outStream = new FancyByteArrayOutputStream();
		outStream.writeFourByteInteger(0xCAFEBABE);
		outStream.writeTwoByteInteger(this.minorVersion);
		outStream.writeTwoByteInteger(this.majorVersion);
		outStream.write(this.constPool.toBytes());
		outStream.writeTwoByteInteger(this.accessFlags.getValue());
		outStream.writeTwoByteInteger(this.thisClassIdentifierIndex);
		outStream.writeTwoByteInteger(this.superClassIdentifierIndex);
		outStream.write(this.interfaces.toBytes());
		outStream.write(this.fields.toBytes());
		outStream.write(this.methods.toBytes());
		outStream.write(this.attributes.toBytes());
		return outStream.toByteArray();
	}
	
	public int getMinorVersion() {
		return this.minorVersion;
	}
	
	public void setMinorVersion(int minorVersion) {
		this.minorVersion = minorVersion;
	}
	
	public int getMajorVersion() {
		return this.majorVersion;
	}
	
	public void setMajorVersion(int majorVersion) {
		this.majorVersion = majorVersion;
	}
	
	public ClassConstantPool getConstantPool() {
		return this.constPool;
	}
	
	public ClassAccessFlags getAccessFlags() {
		return this.accessFlags;
	}
	
	public void setAccessFlags(ClassAccessFlags accessFlags) {
		this.accessFlags = accessFlags;
	}
	
	public int getThisClassIdentifierIndex() {
		return this.thisClassIdentifierIndex;
	}
	
	public String getThisClassIdentifier() {
		return (this.constPool.hasIndex(this.thisClassIdentifierIndex) ?
				this.constPool.get(this.thisClassIdentifierIndex).simpleVal(this.constPool) : null);
	}
	
	public void setThisClassIdentifierIndex(int thisClassIdentifierIndex) {
		this.thisClassIdentifierIndex = thisClassIdentifierIndex;
	}
	
	public int getSuperClassIdentifierIndex() {
		return this.superClassIdentifierIndex;
	}
	
	public String getSuperClassIdentifier() {
		return (this.constPool.hasIndex(this.superClassIdentifierIndex) ?
				this.constPool.get(this.superClassIdentifierIndex).simpleVal(this.constPool) : null);
	}
	
	public void setSuperClassIdentifierIndex(int superClassIdentifierIndex) {
		this.superClassIdentifierIndex = superClassIdentifierIndex;
	}
	
	public ClassInterfaces getInterfaces() {
		return this.interfaces;
	}
	
	public ClassFields getFields() {
		return this.fields;
	}
	
	public ClassMethods getMethods() {
		return this.methods;
	}
	
	public AttributeSet getAttributes() {
		return this.attributes;
	}
	
	@Override
	public String toString() {
		String constPoolStr = "";
		for(int i = 1; i <= this.constPool.size(); i++) {
			ConstantPoolObject obj = this.constPool.get(i);
			constPoolStr += "\n\t\t" + i + ": " + (obj == null ? "~PLACEHOLDER" : obj.val(this.constPool));
		}
		
		String fieldStr = "";
		for(int i = 0; i < this.fields.size(); i++) {
			ClassField field = this.fields.getField(i);
			fieldStr += "\n\t\t" + i + ": " + field.getAccessFlagString() + " " + this.constPool.get(field.getNameIndex()).val(this.constPool)
					+ " " + this.constPool.get(field.getDescIndex()).val(this.constPool);
			if(field.getAttributes().size() != 0) {
				fieldStr += " Attributes:";
				for(int j = 0; j < field.getAttributes().size(); j++) {
					fieldStr += "\n\t\t\t" + j + ": " + field.getAttributes().getAttribute(j).toString(this.constPool);
				}
			}
		}
		
		String attrStr = (this.attributes.size() == 0 ? " {}" : "");
		for(int i = 0; i < this.attributes.size(); i++) {
			attrStr += "\n\t\t" + i + ": " + this.attributes.getAttribute(i).toString(this.constPool);
		}
		
		String methodStr = "";
		for(int i = 0; i < this.methods.size(); i++) {
			ClassMethod method = this.methods.getMethod(i);
			methodStr += "\n\t\t" + i + ": " + method.getAccessFlagString() + " " + this.constPool.get(method.getNameIndex()).val(this.constPool)
					+ " " + this.constPool.get(method.getDescIndex()).val(this.constPool);
			if(method.getAttributes().size() != 0) {
				methodStr += " Attributes:";
				for(int j = 0; j < method.getAttributes().size(); j++) {
					methodStr += "\n\t\t\t" + j + ": " + method.getAttributes().getAttribute(j).toString(this.constPool);
				}
			}
		}
		
		return "ClassFile: {"
				+ "\n\tMinor version: " + this.minorVersion
				+ "\n\tMajor version: " + this.majorVersion
				+ "\n\tConstant pool:" + constPoolStr
				+ "\n\tAccess flags: " + this.accessFlags.toCodeString()
				+ "\n\t\"this\" identifier: " + this.constPool.get(this.thisClassIdentifierIndex).val(this.constPool)
				+ "\n\t\"super\" identifier: " + this.constPool.get(this.superClassIdentifierIndex).val(this.constPool)
				+ "\n\tInterfaces: " + this.interfaces.toString(this.constPool)
				+ "\n\tFields: " + fieldStr // this.fields.toString(this.constPool)
				+ "\n\tMethods: " + methodStr // this.methods.toString(this.constPool)
				+ "\n\tAttributes: " + attrStr // this.attributes.toString(this.constPool)
				+ "\n}";
	}
}
