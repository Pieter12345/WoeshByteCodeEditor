package io.github.pieter12345.wbce;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents access flags of a field.
 * @author P.J.S. Kools
 */
public enum FieldAccessFlag {

	PUBLIC         (0x0001),
	PRIVATE        (0x0002),
	PROTECTED      (0x0004),
	STATIC         (0x0008),
	FINAL          (0x0010),
	RESERVED0x0020 (0x0020),
	VOLATILE       (0x0040),
	TRANSIENT      (0x0080),
	RESERVED0x0100 (0x0100),
	RESERVED0x0200 (0x0200),
	RESERVED0x0400 (0x0400),
	RESERVED0x0800 (0x0800),
	SYNTHETIC      (0x1000),
	RESERVED0x2000 (0x2000),
	ENUM           (0x4000),
	RESERVED0x8000 (0x8000);
	
	private int value;
	
	private FieldAccessFlag(int value) {
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
			case VOLATILE:
			case TRANSIENT:
			case SYNTHETIC:
			case ENUM: {
				return true;
			}
			case RESERVED0x0020:
			case RESERVED0x0100:
			case RESERVED0x0200:
			case RESERVED0x0400:
			case RESERVED0x0800:
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
	 * Gets the set of field access flags for the given value.
	 * @param value - The value representing the field access flags.
	 * If (a part of) the value does not represent an existing access flag, then that part is ignored.
	 * @return A {@link Set} containing all field access flags that are represented by the given value.
	 */
	public static Set<FieldAccessFlag> valueToFlags(int value) {
		Set<FieldAccessFlag> flags = new HashSet<FieldAccessFlag>();
		for(FieldAccessFlag flag : FieldAccessFlag.values()) {
			if((value | flag.value) == value) {
				flags.add(flag);
			}
		}
		return flags;
	}
	
	/**
	 * Gets the value representing the field access flags.
	 * @param flags - A {@link Set} of field access flags.
	 * @return The value representing the field access flags.
	 */
	public static int flagsToValue(Set<FieldAccessFlag> flags) {
		int value = 0;
		for(FieldAccessFlag flag : flags) {
			value |= flag.value;
		}
		return value;
	}
}
