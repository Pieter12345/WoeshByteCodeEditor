package io.github.pieter12345.wbce.attribute;

import io.github.pieter12345.wbce.ClassConstantPool;

public interface IAttribute {
	public byte[] toBytes();
	public String toString(ClassConstantPool constPool);
}
