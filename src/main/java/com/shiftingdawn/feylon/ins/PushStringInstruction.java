package com.shiftingdawn.feylon.ins;

import com.shiftingdawn.feylon.Memory;
import com.shiftingdawn.feylon.MemoryInstruction;
import com.shiftingdawn.feylon.Stack;

public class PushStringInstruction implements MemoryInstruction {

	private final String x;

	public PushStringInstruction(final String x) {
		this.x = x;
	}

	@Override
	public void apply(final Memory memory, final Stack stack) {
		final int pointer = memory.getNextStringPointer();
		final int writtenSize = memory.setString(pointer, this.x);
		stack.push(pointer);
		stack.push(writtenSize);
	}
}
