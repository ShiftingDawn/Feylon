package com.shiftingdawn.stacklang.ins;

import com.shiftingdawn.stacklang.Instruction;
import com.shiftingdawn.stacklang.Stack;

public class PrintInstruction implements Instruction {

	@Override
	public void apply(final Stack stack) {
		System.out.println(stack.pop());
	}
}
