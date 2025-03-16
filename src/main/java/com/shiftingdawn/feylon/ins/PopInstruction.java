package com.shiftingdawn.feylon.ins;

import com.shiftingdawn.feylon.Instruction;
import com.shiftingdawn.feylon.Stack;

public class PopInstruction implements Instruction {

	@Override
	public void apply(final Stack stack) {
		stack.pop();
	}
}
