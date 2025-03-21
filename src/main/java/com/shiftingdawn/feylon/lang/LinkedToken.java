package com.shiftingdawn.feylon.lang;

public final class LinkedToken {

	public final TokenPos pos;
	public final int selfPointer;
	public final InstructionType type;
	public final String txt;
	public Object data;

	public LinkedToken(final TokenPos pos, final int selfPointer, final InstructionType type, final String txt, final Object data) {
		this.pos = pos;
		this.selfPointer = selfPointer;
		this.type = type;
		this.txt = txt;
		this.data = data;
	}
}