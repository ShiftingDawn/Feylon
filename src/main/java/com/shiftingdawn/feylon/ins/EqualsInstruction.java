package com.shiftingdawn.feylon.ins;

import com.shiftingdawn.feylon.Instruction;
import com.shiftingdawn.feylon.Stack;

public class EqualsInstruction implements Instruction {

	@Override
	public void apply(final Stack stack) {
		final double a = stack.pop();
		final double b = stack.pop();
		stack.push(a == b);
	}
}
