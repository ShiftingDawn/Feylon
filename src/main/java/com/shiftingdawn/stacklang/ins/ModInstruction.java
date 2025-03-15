package com.shiftingdawn.stacklang.ins;

import com.shiftingdawn.stacklang.Instruction;
import com.shiftingdawn.stacklang.Stack;

public class ModInstruction implements Instruction {

	@Override
	public void apply(final Stack stack) {
		final int a = stack.pop();
		final int b = stack.pop();
		stack.push(b % a);
	}
}