package io.github.pieter12345.wbce.attribute;

import java.io.IOException;
import java.util.ArrayList;

import io.github.pieter12345.wbce.ClassConstantPool;
import io.github.pieter12345.wbce.FancyByteArrayOutputStream;
import io.github.pieter12345.wbce.FancyInputStream;
import io.github.pieter12345.wbce.constantpool.ConstantPoolObject;
import io.github.pieter12345.wbce.constantpool.ConstantPoolString;

public class AttributeSet {
	
	private ArrayList<IAttribute> attributes;
	
	private AttributeSet(ArrayList<IAttribute> attributes) {
		this.attributes = attributes;
	}
	
	public static AttributeSet fromInputStream(FancyInputStream inStream, ClassConstantPool constPool) throws IOException {
		int attributeCount = inStream.readTwoByteInt();
		ArrayList<IAttribute> attributes = new ArrayList<IAttribute>(attributeCount);
		for(int i = 0; i < attributeCount; i++) {
			
			// TODO - Add an IAttribute implementation for each attribute.
			int attrNameIndex = inStream.readTwoByteInt();
			ConstantPoolObject constPoolObj = constPool.get(attrNameIndex);
			if(constPoolObj instanceof ConstantPoolString) {
				String attributeType = ((ConstantPoolString) constPoolObj).getString();
				IAttribute attr;
				switch(attributeType) {
				case "ConstantValue":
					attr = ConstantValueAttribute.fromInputStream(attrNameIndex, inStream);
					break;
				case "Code":
					attr = CodeAttribute.fromInputStream(attrNameIndex, inStream, constPool);
					break;
				case "StackMapTable":
					attr = StackMapTableAttribute.fromInputStream(attrNameIndex, inStream);
					break;
				case "Exceptions":
					attr = ExceptionsAttribute.fromInputStream(attrNameIndex, inStream);
					break;
				case "InnerClasses":
					attr = InnerClassesAttribute.fromInputStream(attrNameIndex, inStream);
					break;
				case "EnclosingMethod":
					attr = EnclosingMethodAttribute.fromInputStream(attrNameIndex, inStream);
					break;
				case "Synthetic":
					attr = SyntheticAttribute.fromInputStream(attrNameIndex, inStream);
					break;
				case "Signature":
					attr = SignatureAttribute.fromInputStream(attrNameIndex, inStream);
					break;
				case "SourceFile":
					attr = SourceFileAttribute.fromInputStream(attrNameIndex, inStream);
					break;
				case "SourceDebugExtension":
					attr = SourceDebugExtensionAttribute.fromInputStream(attrNameIndex, inStream);
					break;
				case "LineNumberTable":
					attr = LineNumberTableAttribute.fromInputStream(attrNameIndex, inStream);
					break;
				case "LocalVariableTable":
					attr = LocalVariableTableAttribute.fromInputStream(attrNameIndex, inStream);
					break;
				case "LocalVariableTypeTable":
					attr = LocalVariableTypeTableAttribute.fromInputStream(attrNameIndex, inStream);
					break;
				case "Deprecated":
					attr = DeprecatedAttribute.fromInputStream(attrNameIndex, inStream);
					break;
//				case "RuntimeInvisibleAnnotations":
//					attr = RuntimeInvisibleAnnotationsAttribute.fromInputStream(attrNameIndex, inStream);
//					break;
//				case "AnnotationDefault":
//					attr = AnnotationDefaultAttribute.fromInputStream(attrNameIndex, inStream);
//					break;
				case "BootstrapMethods":
					attr = BootstrapMethodsAttribute.fromInputStream(attrNameIndex, inStream);
					break;
				default:
					System.out.println("[DEBUG] [" + AttributeSet.class.getSimpleName()
							+ "] Unimplemented Attribute type found: " + attributeType);
					attr = UnknownAttribute.fromInputStream(attrNameIndex, inStream);
				}
				attributes.add(attr);
			} else {
				throw new RuntimeException("Attribute name index did not point to a ConstantPoolString object.");
			}
		}
		return new AttributeSet(attributes);
	}
	
	public byte[] toBytes() {
		@SuppressWarnings("resource")
		FancyByteArrayOutputStream outStream = new FancyByteArrayOutputStream();
		outStream.writeTwoByteInteger(this.attributes.size());
		for(int i = 0; i < this.attributes.size(); i++) {
			outStream.write(this.attributes.get(i).toBytes());
		}
		return outStream.toByteArray();
	}
	
	public int size() {
		return this.attributes.size();
	}
	
	public IAttribute getAttribute(int index) {
		return this.attributes.get(index);
	}
	
	public IAttribute[] getAttributes() {
		return this.attributes.toArray(new IAttribute[0]);
	}
	
	public void setAttribute(int index, IAttribute classAttribute) {
		this.attributes.set(index, classAttribute);
	}
	
	public void addAttribute(IAttribute classAttribute) {
		this.attributes.add(classAttribute);
	}
	
	public IAttribute removeAttribute(int index) {
		return this.attributes.remove(index);
	}
	
	public boolean removeAttribute(IAttribute classAttribute) {
		return this.attributes.remove(classAttribute);
	}
	
	public String toString(ClassConstantPool constPool) {
		String attributeStr = "AttributeSet: {";
		for(int i = 0; i < this.attributes.size(); i++) {
			IAttribute attribute = this.attributes.get(i);
			attributeStr += i + ": " + attribute.toString(constPool) + " ";
		}
		return attributeStr.trim() + "}";
	}
	
}
