package com.shiftingdawn.stacklang.ins;

import com.shiftingdawn.stacklang.Instruction;
import com.shiftingdawn.stacklang.Stack;

public class PushInstruction implements Instruction {

	private final int x;

	public PushInstruction(final int x) {
		this.x = x;
	}

	@Override
	public void apply(final Stack stack) {
		stack.push(this.x);
	}
}
