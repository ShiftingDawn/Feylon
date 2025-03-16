package com.shiftingdawn.feylon.ins;

import com.shiftingdawn.feylon.Instruction;
import com.shiftingdawn.feylon.Stack;

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
