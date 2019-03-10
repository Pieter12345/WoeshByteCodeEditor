package io.github.pieter12345.wbce.decompile;

public class CodeBlockBounds {
	
	// Variables & Constants.
	private int instrIndex; // The instruction index target.
	private BoundsType type;
	private int causeInstrIndex; // The instruction index of the branch / bleed that causes this bounds or -1.
	
	public CodeBlockBounds(int instrIndex, BoundsType type, int causeInstrIndex) {
		this.instrIndex = instrIndex;
		this.type = type;
		this.causeInstrIndex = causeInstrIndex;
	}
	
	public int getInstrIndex() {
		return this.instrIndex;
	}
	
	public BoundsType getType() {
		return this.type;
	}
	
	public int getCause() {
		return this.causeInstrIndex;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof CodeBlockBounds) {
			CodeBlockBounds other = (CodeBlockBounds) obj;
			return this.instrIndex == other.instrIndex
					&& this.type == other.type && this.causeInstrIndex == other.causeInstrIndex;
		}
		return false;
	}
	
	public static enum BoundsType {
		METHOD_START,
		METHOD_END,
		BRANCH,
		START_PC,
		END_PC,
		HANDLER_PC;
	}
}
