package com.shiftingdawn.stacklang.ins;

import com.shiftingdawn.stacklang.Instruction;
import com.shiftingdawn.stacklang.Stack;

public class NotEqualsInstruction implements Instruction {

	@Override
	public void apply(final Stack stack) {
		final double a = stack.pop();
		final double b = stack.pop();
		stack.push(a != b);
	}
}
