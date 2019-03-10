package io.github.pieter12345.wbce.decompile;

import io.github.pieter12345.wbce.ByteCodeInstruction;
import io.github.pieter12345.wbce.ByteCodeInstruction.ByteCodeInstructionPayload;

public class ByteCodeEntry {
	
	// Variables & Constants.
	private final int byteCodeOffset;
	private final int instructionIndex;
	private final ByteCodeInstruction instruction;
	private final byte[] rawArgBytes;
	private final int[] signedArgs;
	private final int[] unsignedArgs;
	
	/**
	 * Creates a new byte code entry.
	 * @param byteCodeOffset - The byte offset of the instruction in the code.
	 * @param instructionIndex - The instruction number in the code.
	 * @param instruction - The instruction.
	 * @param instructionArgs - The instruction arguments.
	 */
	public ByteCodeEntry(int byteCodeOffset, int instructionIndex, ByteCodeInstruction instruction, byte[] instructionArgs) {
		this.byteCodeOffset = byteCodeOffset;
		this.instructionIndex = instructionIndex;
		this.instruction = instruction;
		this.rawArgBytes = instructionArgs;
		
		this.signedArgs = new int[this.instruction.getPayloadTypes().length];
		this.unsignedArgs = new int[this.instruction.getPayloadTypes().length];
		int index = 0;
		int count = 0;
		for(ByteCodeInstructionPayload payload : this.instruction.getPayloadTypes()) {
			int signedVal = instructionArgs[index] << ((payload.getByteSize() - 1) * 8);
			int unsignedVal = (instructionArgs[index] & 0xFF) << ((payload.getByteSize() - 1) * 8);
			index++;
			for(int j = payload.getByteSize() - 2; j >= 0; j--) {
				signedVal |= ((instructionArgs[index] & 0xFF) << (j * 8));
				unsignedVal |= ((instructionArgs[index] & 0xFF) << (j * 8));
				index++;
			}
			this.signedArgs[count] = signedVal;
			this.unsignedArgs[count] = unsignedVal;
			count++;
		}
	}
	
	/**
	 * Gets the byte offset of the instruction in the code.
	 * @return - The byte offset.
	 */
	public int getOffset() {
		return this.byteCodeOffset;
	}
	
	/**
	 * Gets the instruction number in the code.
	 * @return The instruction number.
	 */
	public int getInstructionIndex() {
		return this.instructionIndex;
	}
	
	public ByteCodeInstruction getInstruction() {
		return this.instruction;
	}
	
	public byte[] getRawInstructionArgBytes() {
		return this.rawArgBytes;
	}
	
	public int[] getSignedInstructionArgs() {
		return this.signedArgs;
	}
	
	public int[] getUnsignedInstructionArgs() {
		return this.unsignedArgs;
	}
	
}
