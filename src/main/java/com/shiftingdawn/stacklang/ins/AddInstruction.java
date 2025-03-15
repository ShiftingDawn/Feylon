package com.shiftingdawn.stacklang.ins;

import com.shiftingdawn.stacklang.Instruction;
import com.shiftingdawn.stacklang.Stack;

public class AddInstruction implements Instruction {

	@Override
	public void apply(final Stack stack) {
		stack.push(stack.pop() + stack.pop());
	}
}
