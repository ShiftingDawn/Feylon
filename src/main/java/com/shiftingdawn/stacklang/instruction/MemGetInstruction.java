package com.shiftingdawn.stacklang.instruction;

import com.shiftingdawn.stacklang.Memory;
import com.shiftingdawn.stacklang.MemoryInstruction;
import com.shiftingdawn.stacklang.Stack;

public class MemGetInstruction implements MemoryInstruction {

	@Override
	public void apply(final Memory memory, final Stack stack) {
		final int pointer = stack.pop();
		final int value = memory.get(pointer);
		stack.push(value);
	}
}
