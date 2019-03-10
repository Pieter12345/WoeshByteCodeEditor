package io.github.pieter12345.wbce;

public enum ByteCodeInstruction {
	nop            ((byte) 0x00, (byte) 0),
	aconst_null    ((byte) 0x01, (byte) 0),
	iconst_m1      ((byte) 0x02, (byte) 0),
	iconst_0       ((byte) 0x03, (byte) 0),
	iconst_1       ((byte) 0x04, (byte) 0),
	iconst_2       ((byte) 0x05, (byte) 0),
	iconst_3       ((byte) 0x06, (byte) 0),
	iconst_4       ((byte) 0x07, (byte) 0),
	iconst_5       ((byte) 0x08, (byte) 0),
	lconst_0       ((byte) 0x09, (byte) 0),
	lconst_1       ((byte) 0x0A, (byte) 0),
	fconst_0       ((byte) 0x0B, (byte) 0),
	fconst_1       ((byte) 0x0C, (byte) 0),
	fconst_2       ((byte) 0x0D, (byte) 0),
	dconst_0       ((byte) 0x0E, (byte) 0),
	dconst_1       ((byte) 0x0F, (byte) 0),
	bipush         ((byte) 0x10, (byte) 1, ByteCodeInstructionPayload.BYTE), // Stack.push((int) byte);
	sipush         ((byte) 0x11, (byte) 2, ByteCodeInstructionPayload.SHORT), // Stack.push(short);
	ldc            ((byte) 0x12, (byte) 1, ByteCodeInstructionPayload.CONSTPOOL_INDEX_BYTE), // Stack.push((String/int/float) constPool.get(byte));
	ldc_w          ((byte) 0x13, (byte) 2, ByteCodeInstructionPayload.CONSTPOOL_INDEX_SHORT), // Stack.push((String/int/float) constPool.get(short));
	ldc2_w         ((byte) 0x14, (byte) 2, ByteCodeInstructionPayload.CONSTPOOL_INDEX_SHORT), // Stack.push((double/long) constPool.get(short));
	iload          ((byte) 0x15, (byte) 1, ByteCodeInstructionPayload.VARIABLE_INDEX_BYTE), // Stack.push((int) localVars.get(byte));
	lload          ((byte) 0x16, (byte) 1, ByteCodeInstructionPayload.VARIABLE_INDEX_BYTE), // Stack.push((long) localVars.get(byte));
	fload          ((byte) 0x17, (byte) 1, ByteCodeInstructionPayload.VARIABLE_INDEX_BYTE), // Stack.push((float) localVars.get(byte));
	dload          ((byte) 0x18, (byte) 1, ByteCodeInstructionPayload.VARIABLE_INDEX_BYTE), // Stack.push((double) localVars.get(byte));
	aload          ((byte) 0x19, (byte) 1, ByteCodeInstructionPayload.VARIABLE_INDEX_BYTE), // Stack.push((objRef) localVars.get(byte));
	iload_0        ((byte) 0x1A, (byte) 0),
	iload_1        ((byte) 0x1B, (byte) 0),
	iload_2        ((byte) 0x1C, (byte) 0),
	iload_3        ((byte) 0x1D, (byte) 0),
	lload_0        ((byte) 0x1E, (byte) 0),
	lload_1        ((byte) 0x1F, (byte) 0),
	lload_2        ((byte) 0x20, (byte) 0),
	lload_3        ((byte) 0x21, (byte) 0),
	fload_0        ((byte) 0x22, (byte) 0),
	fload_1        ((byte) 0x23, (byte) 0),
	fload_2        ((byte) 0x24, (byte) 0),
	fload_3        ((byte) 0x25, (byte) 0),
	dload_0        ((byte) 0x26, (byte) 0),
	dload_1        ((byte) 0x27, (byte) 0),
	dload_2        ((byte) 0x28, (byte) 0),
	dload_3        ((byte) 0x29, (byte) 0),
	aload_0        ((byte) 0x2A, (byte) 0),
	aload_1        ((byte) 0x2B, (byte) 0),
	aload_2        ((byte) 0x2C, (byte) 0),
	aload_3        ((byte) 0x2D, (byte) 0),
	iaload         ((byte) 0x2E, (byte) 0),
	laload         ((byte) 0x2F, (byte) 0),
	faload         ((byte) 0x30, (byte) 0),
	daload         ((byte) 0x31, (byte) 0),
	aaload         ((byte) 0x32, (byte) 0),
	baload         ((byte) 0x33, (byte) 0),
	caload         ((byte) 0x34, (byte) 0),
	saload         ((byte) 0x35, (byte) 0),
	istore         ((byte) 0x36, (byte) 1, ByteCodeInstructionPayload.BYTE), // localVars.set(byte, (int) Stack.pop());
	lstore         ((byte) 0x37, (byte) 1, ByteCodeInstructionPayload.BYTE), // localVars.set(byte, (long) Stack.pop());
	fstore         ((byte) 0x38, (byte) 1, ByteCodeInstructionPayload.BYTE), // localVars.set(byte, (float) Stack.pop());
	dstore         ((byte) 0x39, (byte) 1, ByteCodeInstructionPayload.BYTE), // localVars.set(byte, (double) Stack.pop());
	astore         ((byte) 0x3A, (byte) 1, ByteCodeInstructionPayload.BYTE), // localVars.set(byte, (objRef) Stack.pop());
	istore_0       ((byte) 0x3B, (byte) 0),
	istore_1       ((byte) 0x3C, (byte) 0),
	istore_2       ((byte) 0x3D, (byte) 0),
	istore_3       ((byte) 0x3E, (byte) 0),
	lstore_0       ((byte) 0x3F, (byte) 0),
	lstore_1       ((byte) 0x40, (byte) 0),
	lstore_2       ((byte) 0x41, (byte) 0),
	lstore_3       ((byte) 0x42, (byte) 0),
	fstore_0       ((byte) 0x43, (byte) 0),
	fstore_1       ((byte) 0x44, (byte) 0),
	fstore_2       ((byte) 0x45, (byte) 0),
	fstore_3       ((byte) 0x46, (byte) 0),
	dstore_0       ((byte) 0x47, (byte) 0),
	dstore_1       ((byte) 0x48, (byte) 0),
	dstore_2       ((byte) 0x49, (byte) 0),
	dstore_3       ((byte) 0x4A, (byte) 0),
	astore_0       ((byte) 0x4B, (byte) 0),
	astore_1       ((byte) 0x4C, (byte) 0),
	astore_2       ((byte) 0x4D, (byte) 0),
	astore_3       ((byte) 0x4E, (byte) 0),
	iastore        ((byte) 0x4F, (byte) 0),
	lastore        ((byte) 0x50, (byte) 0),
	fastore        ((byte) 0x51, (byte) 0),
	dastore        ((byte) 0x52, (byte) 0),
	aastore        ((byte) 0x53, (byte) 0),
	bastore        ((byte) 0x54, (byte) 0),
	castore        ((byte) 0x55, (byte) 0),
	sastore        ((byte) 0x56, (byte) 0),
	pop            ((byte) 0x57, (byte) 0),
	pop2           ((byte) 0x58, (byte) 0),
	dup            ((byte) 0x59, (byte) 0),
	dup_x1         ((byte) 0x5A, (byte) 0),
	dup_x2         ((byte) 0x5B, (byte) 0),
	dup2           ((byte) 0x5C, (byte) 0),
	dup2_x1        ((byte) 0x5D, (byte) 0),
	dup2_x2        ((byte) 0x5E, (byte) 0),
	swap           ((byte) 0x5F, (byte) 0),
	iadd           ((byte) 0x60, (byte) 0),
	ladd           ((byte) 0x61, (byte) 0),
	fadd           ((byte) 0x62, (byte) 0),
	dadd           ((byte) 0x63, (byte) 0),
	isub           ((byte) 0x64, (byte) 0),
	lsub           ((byte) 0x65, (byte) 0),
	fsub           ((byte) 0x66, (byte) 0),
	dsub           ((byte) 0x67, (byte) 0),
	imul           ((byte) 0x68, (byte) 0),
	lmul           ((byte) 0x69, (byte) 0),
	fmul           ((byte) 0x6A, (byte) 0),
	dmul           ((byte) 0x6B, (byte) 0),
	idiv           ((byte) 0x6C, (byte) 0),
	ldiv           ((byte) 0x6D, (byte) 0),
	fdiv           ((byte) 0x6E, (byte) 0),
	ddiv           ((byte) 0x6F, (byte) 0),
	irem           ((byte) 0x70, (byte) 0),
	lrem           ((byte) 0x71, (byte) 0),
	frem           ((byte) 0x72, (byte) 0),
	drem           ((byte) 0x73, (byte) 0),
	ineg           ((byte) 0x74, (byte) 0),
	lneg           ((byte) 0x75, (byte) 0),
	fneg           ((byte) 0x76, (byte) 0),
	dneg           ((byte) 0x77, (byte) 0),
	ishl           ((byte) 0x78, (byte) 0),
	lshl           ((byte) 0x79, (byte) 0),
	ishr           ((byte) 0x7A, (byte) 0),
	lshr           ((byte) 0x7B, (byte) 0),
	iushr          ((byte) 0x7C, (byte) 0),
	lushr          ((byte) 0x7D, (byte) 0),
	iand           ((byte) 0x7E, (byte) 0),
	land           ((byte) 0x7F, (byte) 0),
	ior            ((byte) 0x80, (byte) 0),
	lor            ((byte) 0x81, (byte) 0),
	ixor           ((byte) 0x82, (byte) 0),
	lxor           ((byte) 0x83, (byte) 0),
	iinc           ((byte) 0x84, (byte) 2, ByteCodeInstructionPayload.BYTE, ByteCodeInstructionPayload.BYTE), // localVars.set(byte1, localVars.get(byte1) + byte2);
	i2l            ((byte) 0x85, (byte) 0),
	i2f            ((byte) 0x86, (byte) 0),
	i2d            ((byte) 0x87, (byte) 0),
	l2i            ((byte) 0x88, (byte) 0),
	l2f            ((byte) 0x89, (byte) 0),
	l2d            ((byte) 0x8A, (byte) 0),
	f2i            ((byte) 0x8B, (byte) 0),
	f2l            ((byte) 0x8C, (byte) 0),
	f2d            ((byte) 0x8D, (byte) 0),
	d2i            ((byte) 0x8E, (byte) 0),
	d2l            ((byte) 0x8F, (byte) 0),
	d2f            ((byte) 0x90, (byte) 0),
	i2b            ((byte) 0x91, (byte) 0),
	i2c            ((byte) 0x92, (byte) 0),
	i2s            ((byte) 0x93, (byte) 0),
	lcmp           ((byte) 0x94, (byte) 0),
	fcmpl          ((byte) 0x95, (byte) 0),
	fcmpg          ((byte) 0x96, (byte) 0),
	dcmpl          ((byte) 0x97, (byte) 0),
	dcmpg          ((byte) 0x98, (byte) 0),
	ifeq           ((byte) 0x99, (byte) 2, ByteCodeInstructionPayload.SHORT), // If(Stack.pop() == 0) { branch(short) };
	ifne           ((byte) 0x9A, (byte) 2, ByteCodeInstructionPayload.SHORT), // If(Stack.pop() != 0) { branch(short) };
	iflt           ((byte) 0x9B, (byte) 2, ByteCodeInstructionPayload.SHORT), // If(Stack.pop() < 0) { branch(short) };
	ifge           ((byte) 0x9C, (byte) 2, ByteCodeInstructionPayload.SHORT), // If(Stack.pop() >= 0) { branch(short) };
	ifgt           ((byte) 0x9D, (byte) 2, ByteCodeInstructionPayload.SHORT), // If(Stack.pop() > 0) { branch(short) };
	ifle           ((byte) 0x9E, (byte) 2, ByteCodeInstructionPayload.SHORT), // If(Stack.pop() <= 0) { branch(short) };
	if_icmpeq      ((byte) 0x9F, (byte) 2, ByteCodeInstructionPayload.SHORT), // If((int) Stack.pop() == (int) Stack.pop()) { branch(short) };
	if_icmpne      ((byte) 0xA0, (byte) 2, ByteCodeInstructionPayload.SHORT), // If((int) Stack.pop() != (int) Stack.pop()) { branch(short) };
	if_icmplt      ((byte) 0xA1, (byte) 2, ByteCodeInstructionPayload.SHORT), // If(Stack.pop() < Stack.pop()) { branch(short) };
	if_icmpge      ((byte) 0xA2, (byte) 2, ByteCodeInstructionPayload.SHORT), // If(Stack.pop() >= Stack.pop()) { branch(short) };
	if_icmpgt      ((byte) 0xA3, (byte) 2, ByteCodeInstructionPayload.SHORT), // If(Stack.pop() > Stack.pop()) { branch(short) };
	if_icmple      ((byte) 0xA4, (byte) 2, ByteCodeInstructionPayload.SHORT), // If(Stack.pop() <= Stack.pop()) { branch(short) };
	if_acmpeq      ((byte) 0xA5, (byte) 2, ByteCodeInstructionPayload.SHORT), // If((objRef) Stack.pop() == (objRef) Stack.pop()) { branch(short) };
	if_acmpne      ((byte) 0xA6, (byte) 2, ByteCodeInstructionPayload.SHORT), // If((objRef) Stack.pop() != (objRef) Stack.pop()) { branch(short) };
	_goto          ((byte) 0xA7, (byte) 2, ByteCodeInstructionPayload.SHORT), // branch(short);
	jsr            ((byte) 0xA8, (byte) 2, ByteCodeInstructionPayload.SHORT), // branchToSubroutine(short); Stack.push(currentAddress);
	ret            ((byte) 0xA9, (byte) 1, ByteCodeInstructionPayload.VARIABLE_INDEX_BYTE), // Continue execution from locVars.get(byte).
	tableswitch    ((byte) 0xAA, (byte) -1), // 16+?
	lookupswitch   ((byte) 0xAB, (byte) -1),
	// lookupswitch: {<0-3 bytes padding so that the next byte starts at an opcode index which is a multiple of 4>, (int) defaultRelJumpto,
	// (int) numberOfCases, BranchStackFrame[numberOfCases]} where BranchStackFrame: {(int) key, (int) relJumpto}.
	// In case of strings, the keys have value string.hashCode().
	ireturn        ((byte) 0xAC, (byte) 0),
	lreturn        ((byte) 0xAD, (byte) 0),
	freturn        ((byte) 0xAE, (byte) 0),
	dreturn        ((byte) 0xAF, (byte) 0),
	areturn        ((byte) 0xB0, (byte) 0),
	_return        ((byte) 0xB1, (byte) 0),
	getstatic      ((byte) 0xB2, (byte) 2, ByteCodeInstructionPayload.CONSTPOOL_INDEX_SHORT), // Stack.push(constPool.get(short));
	putstatic      ((byte) 0xB3, (byte) 2, ByteCodeInstructionPayload.CONSTPOOL_INDEX_SHORT), // ((StaticFieldRef) constPool.get(short)).set(Stack.pop());
	getfield       ((byte) 0xB4, (byte) 2, ByteCodeInstructionPayload.CONSTPOOL_INDEX_SHORT), // Stack.push(((Class) Stack.pop()).getField((FieldRef) constPool.get(short)));
	putfield       ((byte) 0xB5, (byte) 2, ByteCodeInstructionPayload.CONSTPOOL_INDEX_SHORT), // ((Class) Stack.pop()).setField((FieldRef) constPool.get(short), Stack.pop());
	invokevirtual  ((byte) 0xB6, (byte) 2, ByteCodeInstructionPayload.CONSTPOOL_INDEX_SHORT),
	invokespecial  ((byte) 0xB7, (byte) 2, ByteCodeInstructionPayload.CONSTPOOL_INDEX_SHORT),
	invokestatic   ((byte) 0xB8, (byte) 2, ByteCodeInstructionPayload.CONSTPOOL_INDEX_SHORT),
	invokeinterface((byte) 0xB9, (byte) 4, ByteCodeInstructionPayload.CONSTPOOL_INDEX_SHORT, ByteCodeInstructionPayload.BYTE, ByteCodeInstructionPayload.BYTE),
	invokedynamic  ((byte) 0xBA, (byte) 4, ByteCodeInstructionPayload.CONSTPOOL_INDEX_SHORT, ByteCodeInstructionPayload.BYTE, ByteCodeInstructionPayload.BYTE),
	_new           ((byte) 0xBB, (byte) 2, ByteCodeInstructionPayload.CONSTPOOL_INDEX_SHORT),
	newarray       ((byte) 0xBC, (byte) 1, ByteCodeInstructionPayload.BYTE), // Stack.push(new array(type=byte, size=(int) Stack.pop()));
	// newarray datatypes (possible values of byte arg): 4=boolean, 5=char, 6=float, 7=double, 8=byte, 9=short, 10=int, 11=long.
	anewarray      ((byte) 0xBD, (byte) 2, ByteCodeInstructionPayload.CONSTPOOL_INDEX_SHORT), // Stack.push(new array(type=(Class) constPool.get(short), size=Stack.pop()));
	arraylength    ((byte) 0xBE, (byte) 0),
	athrow         ((byte) 0xBF, (byte) 0),
	checkcast      ((byte) 0xC0, (byte) 2, ByteCodeInstructionPayload.CONSTPOOL_INDEX_SHORT), // Stack.peek() == (classRef) constPool.get(short). Throws ClassCastException?
	_instanceof    ((byte) 0xC1, (byte) 2, ByteCodeInstructionPayload.CONSTPOOL_INDEX_SHORT), // Stack.push(Stack.pop() == (classRef) constPool.get(short));
	monitorenter   ((byte) 0xC2, (byte) 0),
	monitorexit    ((byte) 0xC3, (byte) 0),
	wide           ((byte) 0xC4, (byte) -1), // OPCODE_BYTE, VARIABLE_INDEX_SHORT (Xload, Xstore with short as argument) or iinc_BYTE, VARIABLE_INDEX_SHORT, CONSTPOOL_INDEX_SHORT.
	multianewarray ((byte) 0xC5, (byte) 3, ByteCodeInstructionPayload.CONSTPOOL_INDEX_SHORT, ByteCodeInstructionPayload.BYTE),
	ifnull         ((byte) 0xC6, (byte) 2, ByteCodeInstructionPayload.SHORT), // if(Stack.pop() == null) { branch(short) };
	ifnonnull      ((byte) 0xC7, (byte) 2, ByteCodeInstructionPayload.SHORT), // if(Stack.pop() != null) { branch(short) };
	goto_w         ((byte) 0xC8, (byte) 4, ByteCodeInstructionPayload.INT), // branch(int);
	jsr_w          ((byte) 0xC9, (byte) 4, ByteCodeInstructionPayload.INT), // branchToSubroutine(int); Stack.push(currentAddress);
	breakpoint     ((byte) 0xCA, (byte) 0),
	// 0xCB to 0xFD don't have a name (unused).
	impdep1        ((byte) 0xFE, (byte) 0),
	impdep2        ((byte) 0xFF, (byte) 0);
	
	// Variables & Constants.
	private final byte opCode; // The instruction byte.
	private final byte payload; // The amount of additional bytes (instruction arguments).
	private final ByteCodeInstructionPayload[] payloadTypes;
	
	private ByteCodeInstruction(byte opCode, byte payload, ByteCodeInstructionPayload... payloadTypes) {
		this.opCode = opCode;
		this.payload = payload;
		this.payloadTypes = payloadTypes;
	}
	
	public byte getOpCode() {
		return this.opCode;
	}
	
	public byte getPayload() {
		return this.payload;
	}
	
	public ByteCodeInstructionPayload[] getPayloadTypes() {
		return this.payloadTypes;
	}
	
	public String getInstructionName() {
		return (this.name().charAt(0) == '_' ? this.name().substring(1) : this.name()); // Cut the "_" off if it was there.
	}
	
	public boolean isBranchInstruction() {
		return this == ifeq || this == ifne || this == iflt || this == ifge || this == ifgt || this == iflt
				|| this == if_icmpeq || this == if_icmpne || this == if_icmplt || this == if_icmpge
				|| this == if_icmpgt || this == if_icmple || this == if_acmpeq || this == if_acmpne
				|| this == ifnull || this == ifnonnull || this == _goto || this == jsr || this == goto_w
				|| this == jsr_w;
	}
	
	public boolean isReturn() {
		return this == _return || this == ireturn || this == lreturn || this == freturn || this == dreturn || this == areturn;
	}
	
	public static ByteCodeInstruction forOpCode(byte opCode) {
		for(ByteCodeInstruction instr : ByteCodeInstruction.values()) {
			if(instr.getOpCode() == opCode) {
				return instr;
			}
		}
		return null;
	}
	
	public static ByteCodeInstruction forName(String name) {
		for(ByteCodeInstruction instr : ByteCodeInstruction.values()) {
			if(instr.getInstructionName().equals(name) || instr.getInstructionName().equals("_" + name)) {
				return instr;
			}
		}
		return null;
	}
	
	public static enum ByteCodeInstructionPayload {
		BYTE                 ((byte) 1),
		SHORT                ((byte) 2),
		INT                  ((byte) 4),
		CONSTPOOL_INDEX_BYTE ((byte) 1),
		CONSTPOOL_INDEX_SHORT((byte) 2),
		VARIABLE_INDEX_BYTE  ((byte) 1);
		
		// Variables & Constants.
		private final byte byteSize;
		
		private ByteCodeInstructionPayload(byte byteSize) {
			this.byteSize = byteSize;
		}
		
		public byte getByteSize() {
			return this.byteSize;
		}
		
		public boolean isConstPoolIndex() {
			return this == CONSTPOOL_INDEX_BYTE || this == CONSTPOOL_INDEX_SHORT;
		}
	}
}
