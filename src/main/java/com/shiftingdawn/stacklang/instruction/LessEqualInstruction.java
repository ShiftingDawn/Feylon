package com.shiftingdawn.stacklang.instruction;

import com.shiftingdawn.stacklang.Instruction;
import com.shiftingdawn.stacklang.Stack;

public class LessEqualInstruction implements Instruction {

	@Override
	public void apply(final Stack stack) {
		final double a = stack.pop();
		final double b = stack.pop();
		stack.push(b <= a);
	}
}
