package com.shiftingdawn.stacklang.instruction;

import com.shiftingdawn.stacklang.Instruction;
import com.shiftingdawn.stacklang.Stack;

public class DupInstruction implements Instruction {

	@Override
	public void apply(final Stack stack) {
		final int x = stack.pop();
		stack.push(x);
		stack.push(x);
	}
}
