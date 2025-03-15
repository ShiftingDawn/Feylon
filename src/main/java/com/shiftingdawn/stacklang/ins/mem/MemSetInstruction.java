package com.shiftingdawn.stacklang.ins.mem;

import com.shiftingdawn.stacklang.Memory;
import com.shiftingdawn.stacklang.MemoryInstruction;
import com.shiftingdawn.stacklang.Stack;

public class MemSetInstruction implements MemoryInstruction {

	@Override
	public void apply(final Memory memory, final Stack stack) {
		final int value = stack.pop();
		final int pointer = stack.pop();
		memory.set(pointer, value);
	}
}
