package com.shiftingdawn.stacklang.instruction;

import com.shiftingdawn.stacklang.Instruction;
import com.shiftingdawn.stacklang.Stack;

public class PushInstruction implements Instruction {

	private final int x;

	public PushInstruction(int x) {
		this.x = x;
	}

	@Override
	public void apply(final Stack stack, final Object token) {
		stack.push(this.x);
	}
}
