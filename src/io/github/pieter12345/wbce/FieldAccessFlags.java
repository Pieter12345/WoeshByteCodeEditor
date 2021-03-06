package io.github.pieter12345.wbce;

import java.util.HashSet;
import java.util.Set;

import io.github.pieter12345.wbce.utils.Utils;

/**
 * Represents a set of access flags of a field.
 * @author P.J.S. Kools
 */
public class FieldAccessFlags {
	
	private Set<FieldAccessFlag> flags;
	
	/**
	 * Creates a new {@link FieldAccessFlags} from the raw access flags value.
	 * Access flag data that does not correspond with a field access flag will be ignored.
	 * @param value - The raw access flags value.
	 */
	public FieldAccessFlags(int value) {
		this.flags = FieldAccessFlag.valueToFlags(value);
	}
	
	/**
	 * Gets the access flags.
	 * @return A {@link Set} containing a clone of all access flags.
	 */
	public Set<FieldAccessFlag> getFlags() {
		return new HashSet<FieldAccessFlag>(this.flags);
	}
	
	/**
	 * Sets the access flags.
	 * @param flags - The new access flags.
	 */
	public void setFlags(Set<FieldAccessFlag> flags) {
		this.flags = flags;
	}
	
	/**
	 * Gets the raw access flags value.
	 * @return The raw access flags value.
	 */
	public int getValue() {
		return FieldAccessFlag.flagsToValue(this.flags);
	}
	
	/**
	 * Checks whether the combination of access flags can be valid.
	 * @return {@code true} if the combination of access flags can be valid, {@code false} otherwise.
	 */
	public boolean isValid() {
		
		int accessModifierCount = 0;
		for(FieldAccessFlag flag : this.flags) {
			
			// Check for invalid flags.
			if(!flag.isValid()) {
				return false;
			}
			
			// Count field access modifier flags.
			switch(flag) {
				case PUBLIC:
				case PRIVATE:
				case PROTECTED: {
					accessModifierCount++;
				}
				default: {
					break;
				}
			}
		}
		
		// Check if there is exactly 1 access modifier set.
		if(accessModifierCount != 1) {
			return false;
		}
		
		// Check for invalid flag combinations.
		if(this.flags.contains(FieldAccessFlag.VOLATILE) && this.flags.contains(FieldAccessFlag.FINAL)) {
			return false;
		}
		
		// Return true if no checks have invalidated the access flags.
		return true;
	}
	
	/**
	 * Gets the code string representation of the field access flags.
	 * Note that the result of this is only correct code if the {@link #isValid()} methods returns
	 * {@code true} for this object.
	 * @return The code string representation of the valid field access flags (e.g. "public final"
	 * or "private volatile").
	 * Reserved access flags are ignored.
	 */
	
	public String toCodeString() {
		StringBuilder str = new StringBuilder();
		
		// Access modifiers.
		if(this.flags.contains(FieldAccessFlag.PUBLIC)) { str.append("public "); }
		if(this.flags.contains(FieldAccessFlag.PRIVATE)) { str.append("private "); }
		if(this.flags.contains(FieldAccessFlag.PROTECTED)) { str.append("protected "); }
		
		// Other modifiers.
		if(this.flags.contains(FieldAccessFlag.STATIC)) { str.append("static "); }
		if(this.flags.contains(FieldAccessFlag.FINAL)) { str.append("final "); }
		if(this.flags.contains(FieldAccessFlag.VOLATILE)) { str.append("volatile "); }
		if(this.flags.contains(FieldAccessFlag.TRANSIENT)) { str.append("transient "); }
		
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
