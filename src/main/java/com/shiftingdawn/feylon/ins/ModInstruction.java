package com.shiftingdawn.feylon.ins;

import com.shiftingdawn.feylon.Instruction;
import com.shiftingdawn.feylon.Stack;

public class ModInstruction implements Instruction {

	@Override
	public void apply(final Stack stack) {
		final int a = stack.pop();
		final int b = stack.pop();
		stack.push(b % a);
	}
}