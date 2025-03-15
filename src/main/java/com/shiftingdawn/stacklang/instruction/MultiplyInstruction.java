package com.shiftingdawn.stacklang.instruction;

import com.shiftingdawn.stacklang.Instruction;
import com.shiftingdawn.stacklang.Stack;

public class MultiplyInstruction implements Instruction {

	@Override
	public void apply(final Stack stack, final Object token) {
		stack.push(stack.pop() * stack.pop());
	}
}
