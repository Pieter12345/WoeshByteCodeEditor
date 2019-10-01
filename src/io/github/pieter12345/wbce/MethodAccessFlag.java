package io.github.pieter12345.wbce;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents access flags of a method.
 * @author P.J.S. Kools
 */
public enum MethodAccessFlag {

	PUBLIC         (0x0001),
	PRIVATE        (0x0002),
	PROTECTED      (0x0004),
	STATIC         (0x0008),
	FINAL          (0x0010),
	SYNCHRONIZED   (0x0020),
	BRIDGE         (0x0040),
	VARARGS        (0x0080),
	NATIVE         (0x0100),
	RESERVED0x0200 (0x0200),
	ABSTRACT       (0x0400),
	STRICT         (0x0800),
	SYNTHETIC      (0x1000),
	RESERVED0x2000 (0x2000),
	RESERVED0x4000 (0x4000),
	RESERVED0x8000 (0x8000);
	
	private int value;
	
	private MethodAccessFlag(int value) {
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
			case PUBLIC:
			case PRIVATE:
			case PROTECTED:
			case STATIC:
			case FINAL:
			case SYNCHRONIZED:
			case BRIDGE:
			case VARARGS:
			case NATIVE:
			case ABSTRACT:
			case STRICT:
			case SYNTHETIC: {
				return true;
			}
			case RESERVED0x0200:
			case RESERVED0x4000:
			case RESERVED0x2000:
			case RESERVED0x8000: {
				return false;
			}
			default: {
				throw new Error("Unsupported flag: " + this.name());
			}
		}
	}
	
	/**
	 * Gets the set of method access flags for the given value.
	 * @param value - The value representing the method access flags.
	 * If (a part of) the value does not represent an existing access flag, then that part is ignored.
	 * @return A {@link Set} containing all method access flags that are represented by the given value.
	 */
	public static Set<MethodAccessFlag> valueToFlags(int value) {
		Set<MethodAccessFlag> flags = new HashSet<MethodAccessFlag>();
		for(MethodAccessFlag flag : MethodAccessFlag.values()) {
			if((value | flag.value) == value) {
				flags.add(flag);
			}
		}
		return flags;
	}
	
	/**
	 * Gets the value representing the method access flags.
	 * @param flags - A {@link Set} of method access flags.
	 * @return The value representing the method access flags.
	 */
	public static int flagsToValue(Set<MethodAccessFlag> flags) {
		int value = 0;
		for(MethodAccessFlag flag : flags) {
			value |= flag.value;
		}
		return value;
	}
}
