package com.shiftingdawn.stacklang.instruction;

import com.shiftingdawn.stacklang.Instruction;
import com.shiftingdawn.stacklang.Stack;

public class PrintInstruction implements Instruction {

	@Override
	public void apply(final Stack stack, final Object token) {
		System.out.println(stack.pop());
	}
}
