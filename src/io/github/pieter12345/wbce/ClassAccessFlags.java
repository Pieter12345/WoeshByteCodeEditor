package io.github.pieter12345.wbce;

import java.util.Set;

import io.github.pieter12345.wbce.utils.Utils;

/**
 * Represents a set of access flags of a class.
 * @author P.J.S. Kools
 */
public class ClassAccessFlags {
	
	private Set<ClassAccessFlag> flags;
	
	/**
	 * Creates a new {@link ClassAccessFlags} from the raw access flags value.
	 * Access flag data that does not correspond with a class access flag will be ignored.
	 * @param value - The raw access flags value.
	 */
	public ClassAccessFlags(int value) {
		this.flags = ClassAccessFlag.valueToFlags(value);
	}
	
	/**
	 * Gets the raw access flags value.
	 * @return The raw access flags value.
	 */
	public int getValue() {
		return ClassAccessFlag.flagsToValue(this.flags);
	}
	
	/**
	 * Checks whether the combination of access flags is valid.
	 * @return {@code true} if the combination of access flags, {@code false} otherwise.
	 */
	public boolean isValid() {
		int classTypeCount = 0;
		for(ClassAccessFlag flag : this.flags) {
			
			// Check for invalid flags.
			if(!flag.isValid()) {
				return false;
			}
			
			// Count class type flags.
			switch(flag) {
				case SUPER: // "class".
				case INTERFACE:
				case ANNOTATION:
				case ENUM: {
					classTypeCount++;
				}
				default: {
					break;
				}
			}
		}
		
		// Check if there is exactly 1 class type set.
		if(classTypeCount != 1) {
			return false;
		}
		
		// Check for invalid flag combinations.
		if(this.flags.contains(ClassAccessFlag.ABSTRACT) && this.flags.contains(ClassAccessFlag.FINAL)) {
			return false;
		}
		
		// Return true if no checks have invalidated the access flags.
		return true;
	}
	
	/**
	 * Gets the code string representation of the class access flags.
	 * Note that the result of this is only correct code if the {@link #isValid()} methods returns
	 * {@code true} for this object.
	 * @return The code string representation of the valid class access flags (e.g. "public final class"
	 * or "private interface"). Returns "class" if no valid access flags were set.
	 * Reserved access flags are ignored.
	 */
	public String toCodeString() {
		StringBuilder str = new StringBuilder();
		
		// Modifiers.
		if(this.flags.contains(ClassAccessFlag.PUBLIC)) { str.append("public "); }
		if(this.flags.contains(ClassAccessFlag.FINAL)) { str.append("final "); }
		if(this.flags.contains(ClassAccessFlag.ABSTRACT)) { str.append("abstract "); }
		
		// Class type.
		if(this.flags.contains(ClassAccessFlag.SUPER)) { str.append("class "); }
		if(this.flags.contains(ClassAccessFlag.INTERFACE)) { str.append("interface "); }
		if(this.flags.contains(ClassAccessFlag.ANNOTATION)) { str.append("annotation "); }
		if(this.flags.contains(ClassAccessFlag.ENUM)) { str.append("enum "); }
		
		// Cut trailing whitespace.
		if(str.length() != 0) {
			str.deleteCharAt(str.length() - 1);
		}
		
		// Return the result.
		return str.toString();
	}
	
	@Override
	public String toString() {
		return this.getClass().getSimpleName()
				+ "{" + Utils.glueIterable(this.flags, (flag) -> flag.toString(), ", ") + "}";
	}
}
