package com.shiftingdawn.feylon.ins;

import com.shiftingdawn.feylon.Instruction;
import com.shiftingdawn.feylon.Stack;

public class PrintInstruction implements Instruction {

	@Override
	public void apply(final Stack stack) {
		System.out.println(stack.pop());
	}
}
