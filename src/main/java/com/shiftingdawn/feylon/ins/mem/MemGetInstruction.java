package com.shiftingdawn.feylon.ins.mem;

import com.shiftingdawn.feylon.Memory;
import com.shiftingdawn.feylon.MemoryInstruction;
import com.shiftingdawn.feylon.Stack;

public class MemGetInstruction implements MemoryInstruction {

	@Override
	public void apply(final Memory memory, final Stack stack) {
		final int pointer = stack.pop();
		final int value = memory.get(pointer);
		stack.push(value);
	}
}
