package io.github.pieter12345.wbce;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents access flags of a class.
 * @author P.J.S. Kools
 */
public enum ClassAccessFlag {

	PUBLIC         (0x0001),
	RESERVED0x0002 (0x0002),
	RESERVED0x0004 (0x0004),
	RESERVED0x0008 (0x0008),
	FINAL          (0x0010),
	SUPER          (0x0020),
	RESERVED0x0040 (0x0040),
	RESERVED0x0080 (0x0080),
	RESERVED0x0100 (0x0100),
	INTERFACE      (0x0200),
	ABSTRACT       (0x0400),
	RESERVED0x0800 (0x0800),
	SYNTHETIC      (0x1000),
	ANNOTATION     (0x2000),
	ENUM           (0x4000),
	RESERVED0x8000 (0x8000);
	
	private int value;
	
	private ClassAccessFlag(int value) {
		this.value = value;
	}
	
	/**
	 * Gets the raw value of this access flag.
	 * @return The raw value of this access flag.
	 */
	public int getValue() {
		return this.value;
	}
	
	/**
	 * Gets whether this flag is supported in JVM implementations (as of ~2019).
	 * @return {@code true} if the flag is supported, {@code false} otherwise.
	 */
	public boolean isValid() {
		switch(this) {
			case ABSTRACT:
			case ANNOTATION:
			case ENUM:
			case FINAL:
			case INTERFACE:
			case PUBLIC:
			case SUPER:
			case SYNTHETIC: {
				return true;
			}
			case RESERVED0x0002:
			case RESERVED0x0004:
			case RESERVED0x0008:
			case RESERVED0x0040:
			case RESERVED0x0080:
			case RESERVED0x0100:
			case RESERVED0x0800:
			case RESERVED0x8000: {
				return false;
			}
			default: {
				throw new Error("Unsupported flag: " + this.name());
			}
		}
	}
	
	/**
	 * Gets the set of class access flags for the given value.
	 * @param value - The value representing the class access flags.
	 * If (a part of) the value does not represent an existing access flag, then that part is ignored.
	 * @return A {@link Set} containing all class access flags that are represented by the given value.
	 */
	public static Set<ClassAccessFlag> valueToFlags(int value) {
		Set<ClassAccessFlag> flags = new HashSet<ClassAccessFlag>();
		for(ClassAccessFlag flag : ClassAccessFlag.values()) {
			if((value | flag.value) == value) {
				flags.add(flag);
			}
		}
		return flags;
	}
	
	/**
	 * Gets the value representing the class access flags.
	 * @param flags - A {@link Set} of class access flags.
	 * @return The value representing the class access flags.
	 */
	public static int flagsToValue(Set<ClassAccessFlag> flags) {
		int value = 0;
		for(ClassAccessFlag flag : flags) {
			value |= flag.value;
		}
		return value;
	}
}
