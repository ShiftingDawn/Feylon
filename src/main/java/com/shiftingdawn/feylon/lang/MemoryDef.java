package com.shiftingdawn.feylon.lang;

public final class MemoryDef {

	public final TokenPos pos;
	public final int memSize;
	public int offset;

	public MemoryDef(final TokenPos pos, final int memSize) {
		this.pos = pos;
		this.memSize = memSize;
	}
}
