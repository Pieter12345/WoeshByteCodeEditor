package io.github.pieter12345.wbce.decompile;

public class CodeLabel {
	
	// Variables & Constants.
	private int id;
	private LabelType type;
	
	public CodeLabel(int id, LabelType type) {
		this.id = id;
		this.type = type;
	}
	
	public int getId() {
		return this.id;
	}
	
	public LabelType getType() {
		return this.type;
	}
	
	@Override
	public boolean equals(Object other) {
		return other instanceof CodeLabel && ((CodeLabel) other).id == this.id && ((CodeLabel) other).type == this.type;
	}
	
	/**
	 * toString method.
	 * @return A String representing this CodeLabel.
	 * Examples for id == 1: "L1" (BRANCH_LABEL), "CATCH_L1" (CATCH_LABEL) or "LOOP_L1" (LOOP_LABEL).
	 */
	@Override
	public String toString() {
		switch(this.type) {
		case BRANCH_LABEL:
			return "L" + this.id;
		case CATCH_LABEL:
			return "CATCH_L" + this.id;
		case LOOP_LABEL:
			return "LOOP_L" + this.id;
		default:
			throw new RuntimeException("Found unimplemented LabelType: " + this.type.toString());
		}
	}
	
	public static enum LabelType {
		BRANCH_LABEL,
		CATCH_LABEL,
		LOOP_LABEL;
	}
}
