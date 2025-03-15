package com.shiftingdawn.stacklang.instruction;

import com.shiftingdawn.stacklang.Instruction;
import com.shiftingdawn.stacklang.Stack;

public class EqualsInstruction implements Instruction {

	@Override
	public void apply(final Stack stack, final Object token) {
		final double a = stack.pop();
		final double b = stack.pop();
		stack.push(a == b);
	}
}
