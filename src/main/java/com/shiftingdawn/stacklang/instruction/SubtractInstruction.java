package com.shiftingdawn.stacklang.instruction;

import com.shiftingdawn.stacklang.Instruction;
import com.shiftingdawn.stacklang.Stack;

public class SubtractInstruction implements Instruction {

	@Override
	public void apply(final Stack stack, final Object token) {
		final int a = stack.pop();
		final int b = stack.pop();
		stack.push(b - a);
	}
}
