package io.github.pieter12345.wbce;

import java.io.IOException;
import java.util.ArrayList;

import io.github.pieter12345.wbce.constantpool.ConstantPoolClassRef;
import io.github.pieter12345.wbce.constantpool.ConstantPoolDouble;
import io.github.pieter12345.wbce.constantpool.ConstantPoolFieldRef;
import io.github.pieter12345.wbce.constantpool.ConstantPoolFloat;
import io.github.pieter12345.wbce.constantpool.ConstantPoolInteger;
import io.github.pieter12345.wbce.constantpool.ConstantPoolInterfaceRef;
import io.github.pieter12345.wbce.constantpool.ConstantPoolInvokeDynamic;
import io.github.pieter12345.wbce.constantpool.ConstantPoolLong;
import io.github.pieter12345.wbce.constantpool.ConstantPoolMethodHandle;
import io.github.pieter12345.wbce.constantpool.ConstantPoolMethodRef;
import io.github.pieter12345.wbce.constantpool.ConstantPoolMethodType;
import io.github.pieter12345.wbce.constantpool.ConstantPoolNameAndTypeDesc;
import io.github.pieter12345.wbce.constantpool.ConstantPoolObject;
import io.github.pieter12345.wbce.constantpool.ConstantPoolString;
import io.github.pieter12345.wbce.constantpool.ConstantPoolStringRef;

public class ClassConstantPool {
	
	// Variables & Constants.
	private ArrayList<ConstantPoolObject> constantPool;
	
	private ClassConstantPool(ArrayList<ConstantPoolObject> constantPool) {
		this.constantPool = constantPool;
	}
	
	public static ClassConstantPool fromInputStream(FancyInputStream inStream) throws IOException {
		ArrayList<ConstantPoolObject> constantPool = new ArrayList<ConstantPoolObject>();
		int poolCount = inStream.readTwoByteInt();
		for(int i = 1; i < poolCount; i++) {
			byte tagByte = inStream.readByte();
			ConstantPoolType type = ConstantPoolType.getTypeFromTag(tagByte);
			if(type == null) {
				System.out.println("[ClassConstantPool] Found invalid/unsupported Constant Pool tag:"
						+ tagByte + " on index: " + i + ". Returning null since we don't know how much to read.");
				return null;
			}
			ConstantPoolObject val;
			switch(type) {
			case STRING:
				val = ConstantPoolString.fromInputStream(inStream);
				break;
			case INTEGER:
				val = ConstantPoolInteger.fromInputStream(inStream);
				break;
			case FLOAT:
				val = ConstantPoolFloat.fromInputStream(inStream);
				break;
			case LONG:
				val = ConstantPoolLong.fromInputStream(inStream);
				break;
			case DOUBLE:
				val = ConstantPoolDouble.fromInputStream(inStream);
				break;
			case CLASS_REF:
				val = ConstantPoolClassRef.fromInputStream(inStream);
				break;
			case STRING_REF:
				val = ConstantPoolStringRef.fromInputStream(inStream);
				break;
			case FIELD_REF:
				val = ConstantPoolFieldRef.fromInputStream(inStream);
				break;
			case METHOD_REF:
				val = ConstantPoolMethodRef.fromInputStream(inStream);
				break;
			case INTERFACE_REF:
				val = ConstantPoolInterfaceRef.fromInputStream(inStream);
				break;
			case NAME_AND_TYPE_DESC:
				val = ConstantPoolNameAndTypeDesc.fromInputStream(inStream);
				break;
			case METHOD_HANDLE:
				val = ConstantPoolMethodHandle.fromInputStream(inStream);
				break;
			case METHOD_TYPE:
				val = ConstantPoolMethodType.fromInputStream(inStream);
				break;
			case INVOKEDYNAMIC:
				val = ConstantPoolInvokeDynamic.fromInputStream(inStream);
				break;
			default:
				val = null;
				System.out.println("[ClassConstantPool] Found invalid/unsupported Constant Pool tag:"
						+ tagByte + " on index: " + i + ". Returning null since we don't know how much to read.");
				return null;
			}
			constantPool.add(val);
			
			// Handle double indices (Longs and Doubles use these).
			if(type == ConstantPoolType.LONG || type == ConstantPoolType.DOUBLE) {
				constantPool.add(null); // Placeholder.
				i++;
			}
//			System.out.println("[DEBUG] [ClassConstantPool] " + i + ": " + val.val(null));
		}
		return new ClassConstantPool(constantPool);
	}
	
	public byte[] toBytes() {
		@SuppressWarnings("resource")
		FancyByteArrayOutputStream outStream = new FancyByteArrayOutputStream();
		outStream.writeTwoByteInteger(this.constantPool.size() + 1); // One more than the actual size.
		for(int i = 0; i < this.constantPool.size(); i++) {
			ConstantPoolObject obj = this.constantPool.get(i);
			if(obj != null) { // Null check required to remove placeholders for Doubles and Longs.
				outStream.write(obj.toBytes());
			}
		}
		return outStream.toByteArray();
	}
	
	/**
	 * hasIndex method.
	 * @param index The index to check the existence of.
	 * @return True if the index is in range [1, const pool length]. False otherwise.
	 */
	public boolean hasIndex(int index) {
		return this.constantPool != null && index > 0 && index <= this.constantPool.size();
	}
	
	/**
	 * get method.
	 * Gets a value from the constant pool.
	 * @param index The index in range [1, const pool length].
	 * @return The ConstantPoolObject at the given index.
	 * @throws IndexOutOfBoundsException
	 */
	public ConstantPoolObject get(int index) throws IndexOutOfBoundsException {
		try {
			return this.constantPool.get(index - 1);
		} catch(IndexOutOfBoundsException e) {
			throw new IndexOutOfBoundsException("Expecting the constant pool index to be in range [1, const pool size]."
					+ " Size: " + this.constantPool.size() + " , Received: " + index);
		}
	}
	
	/**
	 * get method.
	 * Gets a value from the constant pool and matches it with the given class.
	 * @param index The index in range [1, const pool length].
	 * @param valueClass The class the index is expected to have.
	 * @return The ConstantPoolObject at the given index.
	 * @throws IndexOutOfBoundsException
	 * @throws ClassCastException
	 */
	@SuppressWarnings("unchecked")
	public <T extends ConstantPoolObject> T get(int index, Class<T> valueClass)
			throws IndexOutOfBoundsException, ClassCastException {
		ConstantPoolObject obj = this.get(index);
//		if(obj == null || (valueClass != null && valueClass.isAssignableFrom(obj.getClass()))) {
		if(obj == null || (valueClass != null && valueClass.equals(obj.getClass()))) {
			return (T) obj;
		}
		throw new ClassCastException("Cannot cast " + (obj == null ? "null" : obj.getClass().getSimpleName())
				+ " to " + (valueClass == null ? "null" : valueClass.getSimpleName()));
	}
	
	/**
	 * set method.
	 * Sets a value in the constant pool.
	 * @param index The index in range [1, const pool length].
	 * @param constPoolObj The constant pool object to set.
	 */
	public void set(int index, ConstantPoolObject constPoolObj) {
		this.constantPool.set(index - 1, constPoolObj);
	}
	
	/**
	 * add method.
	 * Adds a value to the constant pool.
	 * @param constPoolObj The constant pool object to set.
	 * @return The index of the object in the constant pool.
	 */
	public int add(ConstantPoolObject constPoolObj) {
		this.constantPool.add(constPoolObj);
		return this.constantPool.size();
	}
	
	/**
	 * size method.
	 * @return The size of the constant pool.
	 */
	public int size() {
		return this.constantPool.size();
	}
	
	/**
	 * ConstantPoolType enum.
	 * Contains all possible constant pool data types with their matching identifier (tag) byte.
	 */
	public static enum ConstantPoolType {
		STRING            ((byte) 1),
		INTEGER           ((byte) 3),
		FLOAT             ((byte) 4),
		LONG              ((byte) 5),
		DOUBLE            ((byte) 6),
		CLASS_REF         ((byte) 7),
		STRING_REF        ((byte) 8),
		FIELD_REF         ((byte) 9),
		METHOD_REF        ((byte) 10),
		INTERFACE_REF     ((byte) 11),
		NAME_AND_TYPE_DESC((byte) 12),
		METHOD_HANDLE     ((byte) 15),
		METHOD_TYPE       ((byte) 16),
		INVOKEDYNAMIC     ((byte) 18);
		
		private final byte tag;
		
		private ConstantPoolType(byte tag) {
			this.tag = tag;
		}
		
		public byte getTagByte() {
			return this.tag;
		}
		
		public static ConstantPoolType getTypeFromTag(byte tag) {
			for(ConstantPoolType type : ConstantPoolType.values()) {
				if(type.getTagByte() == tag) {
					return type;
				}
			}
			return null;
		}
	}
}
