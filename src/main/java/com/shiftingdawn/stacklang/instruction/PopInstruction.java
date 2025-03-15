package com.shiftingdawn.stacklang.instruction;

import com.shiftingdawn.stacklang.Instruction;
import com.shiftingdawn.stacklang.Stack;

public class PopInstruction implements Instruction {

	@Override
	public void apply(final Stack stack) {
		stack.pop();
	}
}
