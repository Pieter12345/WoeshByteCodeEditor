package io.github.pieter12345.wbce.decompile;

/**
 * Represents a code loop (for / while / dowhile).
 * @author P.J.S. Kools
 */
public class CodeLoop {
	
	private final int startInstrIndex;
	private final int conditionStartInstrIndex;
	private final int endInstrIndex;
	private final int breakTargetInstrIndex;
	
	/**
	 * Creates a new CodeLoop.
	 * @param startInstrIndex - The index of the first instruction in the loop.
	 * This is the instruction that can be branched to from the endInstrIndex to cause the loop.
	 * @param conditionStartInstrIndex - The index of the first condition instruction in the loop. The last instruction
	 * of the condition is the endInstrIndex.
	 * @param endInstrIndex - The index of the last instruction in the loop.
	 * @param breakTargetInstrIndex - The index a "break" would branch to.
	 * This is the branch instruction that causes the loop.
	 */
	public CodeLoop(int startInstrIndex, int conditionStartInstrIndex, int endInstrIndex, int breakTargetInstrIndex) {
		assert startInstrIndex <= conditionStartInstrIndex;
		assert conditionStartInstrIndex <= endInstrIndex;
		assert endInstrIndex < breakTargetInstrIndex;
		this.startInstrIndex = startInstrIndex;
		this.conditionStartInstrIndex = conditionStartInstrIndex;
		this.endInstrIndex = endInstrIndex;
		this.breakTargetInstrIndex = breakTargetInstrIndex;
	}
	
	public int getStartInstrIndex() {
		return this.startInstrIndex;
	}
	
	public int getConditionStartInstrIndex() {
		return this.conditionStartInstrIndex;
	}
	
	public int getEndInstrIndex() {
		return this.endInstrIndex;
	}
	
	public int getBreakTargetInstrIndex() {
		return this.breakTargetInstrIndex;
	}
	
	@Override
	public String toString() {
		return CodeLoop.class.getSimpleName() + "{start=" + this.startInstrIndex
				+ ", condition=" + this.conditionStartInstrIndex + ", end=" + this.endInstrIndex
				+ ", breakTarget=" + this.breakTargetInstrIndex + "}";
	}
}
