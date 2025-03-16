package com.shiftingdawn.feylon.ins.mem;

import com.shiftingdawn.feylon.Memory;
import com.shiftingdawn.feylon.MemoryInstruction;
import com.shiftingdawn.feylon.Stack;

public class MemSetInstruction implements MemoryInstruction {

	@Override
	public void apply(final Memory memory, final Stack stack) {
		final int value = stack.pop();
		final int pointer = stack.pop();
		memory.setInt(pointer, value);
	}
}
