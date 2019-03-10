package io.github.pieter12345.wbce.constantpool;

import io.github.pieter12345.wbce.ClassConstantPool;
import io.github.pieter12345.wbce.ClassConstantPool.ConstantPoolType;

public interface ConstantPoolObject {
	public ConstantPoolType getType();
	public byte[] toBytes();
	public String val(ClassConstantPool constPool);
	public String simpleVal(ClassConstantPool constPool);
}
