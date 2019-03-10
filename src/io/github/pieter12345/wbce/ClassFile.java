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

public class ClassFile {
	
	// Variables & Constants.
	private int minorVersion = -1;
	private int majorVersion = -1;
	private ClassConstantPool constPool = null;
	private int accessFlags = -1;
	private int thisClassIdentifierIndex = -1;
	private int superClassIdentifierIndex = -1;
	private ClassInterfaces interfaces = null;
	private ClassFields fields = null;
	private ClassMethods methods = null;
	private AttributeSet attributes = null;
	
	private final File classFile;
	
	private boolean fileRead = false;
	
	
	public ClassFile(File classFile) {
		this.classFile = classFile;
	}
	
	public void readFile() throws FileNotFoundException, IOException, Exception {
		FancyInputStream inStream = new FancyInputStream(new FileInputStream(this.classFile));
		
		// Get the magic value (CAFEBABE, 4 bytes as 8 bit integers) to verify that it's a class file.
		int magicValue = inStream.readFourByteInt();
		if(magicValue != 0xCAFEBABE) {
			inStream.close();
			throw new Exception("File is not a class file.");
		}
		
		// Get the minor version (2 bytes, big edian).
		this.minorVersion = inStream.readTwoByteInt();
//		System.out.println("Minor version: " + this.minorVersion);
		
		// Get the major version (2 bytes, big edian).
		this.majorVersion = inStream.readTwoByteInt();
//		System.out.println("Major version: " + this.majorVersion);
		
		// Get the constant pool.
		this.constPool = ClassConstantPool.fromInputStream(inStream);
//		System.out.println("Constant pool size: " + this.constPool.size());
		
		// Get the access flags (2 bytes bitmask).
		this.accessFlags = inStream.readTwoByteInt();
//		{
//			boolean classPublic     = (accessFlags & 0x0001) == 0x0001;
//			boolean classFinal      = (accessFlags & 0x0010) == 0x0010;
//			boolean classSuper      = (accessFlags & 0x0020) == 0x0020;
//			boolean classInterface  = (accessFlags & 0x0200) == 0x0200;
//			boolean classAbstract   = (accessFlags & 0x0400) == 0x0400;
//			boolean classSynthetic  = (accessFlags & 0x1000) == 0x1000;
//			boolean classAnnotation = (accessFlags & 0x2000) == 0x2000;
//			boolean classEnum       = (accessFlags & 0x4000) == 0x4000;
//			System.out.println("Access flags: " + accessFlags + " -> "
//					+ (classPublic ? "public " : "") + (classFinal ? "final " : "")
//					+ (classSuper ? "super " : "") + (classInterface ? "interface " : "")
//					+ (classAbstract ? "abstract " : "") + (classSynthetic ? "synthetic " : "")
//					+ (classAnnotation ? "annotation " : "") + (classEnum ? "enum " : "")
//				);
//		}
		
		// Get class identifier (index in constant pool).
		this.thisClassIdentifierIndex = inStream.readTwoByteInt();
//		System.out.println("Class identifier index: " + this.thisClassIdentifierIndex
//				+ "->" + this.constPool.get(this.thisClassIdentifierIndex).val(this.constPool));
		
		// Get super class identifier (index in constant pool).
		this.superClassIdentifierIndex = inStream.readTwoByteInt();
//		System.out.println("Super class identifier index: " + this.superClassIdentifierIndex
//				+ "->" + this.constPool.get(this.superClassIdentifierIndex).val(this.constPool));
		
		// Get the interfaces.
		this.interfaces = ClassInterfaces.fromInputStream(inStream);
//		System.out.println("Interface count: " + this.interfaces.size());

		// Get the fields.
		this.fields = ClassFields.fromInputStream(inStream, this.constPool);
//		System.out.println("Field count: " + this.fields.size());
		
		// Get the methods.
		this.methods = ClassMethods.fromInputStream(inStream, this.constPool);
//		System.out.println("Method count: " + this.methods.size());
		
		// Get the class attributes.
		this.attributes = AttributeSet.fromInputStream(inStream, this.constPool);
//		System.out.println("Attribute count: " + this.attributes.size());
		
		// Check if the end of the file has been reached.
		if(inStream.read() != -1) {
			System.out.println("[Warning] [ClassFile] The class file contains more bytes then expected.");
		}
		
		this.fileRead = true;
	}
	
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
		byte[] classBytes = this.toBytes();
		outStream.write(classBytes);
		outStream.close();
	}
	
	private byte[] toBytes() {
		@SuppressWarnings("resource")
		FancyByteArrayOutputStream outStream = new FancyByteArrayOutputStream();
		outStream.writeFourByteInteger(0xCAFEBABE);
		outStream.writeTwoByteInteger(this.minorVersion);
		outStream.writeTwoByteInteger(this.majorVersion);
		outStream.write(this.constPool.toBytes());
		outStream.writeTwoByteInteger(this.accessFlags);
		outStream.writeTwoByteInteger(this.thisClassIdentifierIndex);
		outStream.writeTwoByteInteger(this.superClassIdentifierIndex);
		outStream.write(this.interfaces.toBytes());
		outStream.write(this.fields.toBytes());
		outStream.write(this.methods.toBytes());
		outStream.write(this.attributes.toBytes());
		return outStream.toByteArray();
	}
	
	/**
	 * isRead method.
	 * @return True if the readFile() method successfully read the file. False otherwise.
	 */
	public boolean isRead() {
		return this.fileRead;
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
	
	public int getAccessFlags() {
		return this.accessFlags;
	}
	
	public void setAccessFlags(int accessFlags) {
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
	
	public File getFile() {
		return this.classFile;
	}
	
	@Override
	public String toString() {
		if(!this.fileRead) {
			return super.toString();
		}
		
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
		
		String accessFlagStr = "";
		boolean classPublic     = (this.accessFlags & 0x0001) == 0x0001;
		boolean classFinal      = (this.accessFlags & 0x0010) == 0x0010;
		boolean classSuper      = (this.accessFlags & 0x0020) == 0x0020;
		boolean classInterface  = (this.accessFlags & 0x0200) == 0x0200;
		boolean classAbstract   = (this.accessFlags & 0x0400) == 0x0400;
		boolean classSynthetic  = (this.accessFlags & 0x1000) == 0x1000;
		boolean classAnnotation = (this.accessFlags & 0x2000) == 0x2000;
		boolean classEnum       = (this.accessFlags & 0x4000) == 0x4000;
		if(classPublic) { accessFlagStr += "public "; }
		if(classFinal) { accessFlagStr += "final "; }
		if(classSuper) { accessFlagStr += "super "; }
		if(classInterface) { accessFlagStr += "interface "; }
		if(classAbstract) { accessFlagStr += "abstract "; }
		if(classSynthetic) { accessFlagStr += "synthetic "; }
		if(classAnnotation) { accessFlagStr += "annotation "; }
		if(classEnum) { accessFlagStr += "enum "; }
		
		return "ClassFile: {"
				+ "\n\tMinor version: " + this.minorVersion
				+ "\n\tMajor version: " + this.majorVersion
				+ "\n\tConstant pool:" + constPoolStr
				+ "\n\tAccess flags: " + accessFlagStr.trim()
				+ "\n\t\"this\" identifier: " + this.constPool.get(this.thisClassIdentifierIndex).val(this.constPool)
				+ "\n\t\"super\" identifier: " + this.constPool.get(this.superClassIdentifierIndex).val(this.constPool)
				+ "\n\tInterfaces: " + this.interfaces.toString(this.constPool)
				+ "\n\tFields: " + fieldStr // this.fields.toString(this.constPool)
				+ "\n\tMethods: " + methodStr // this.methods.toString(this.constPool)
				+ "\n\tAttributes: " + attrStr // this.attributes.toString(this.constPool)
				+ "\n}";
	}
}
