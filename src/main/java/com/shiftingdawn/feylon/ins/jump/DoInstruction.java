package com.shiftingdawn.feylon.ins.jump;

import com.shiftingdawn.feylon.JumpInstruction;
import com.shiftingdawn.feylon.Stack;

import java.util.function.IntConsumer;

public class DoInstruction implements JumpInstruction {

	private final int pointer;

	public DoInstruction(final int pointer) {
		this.pointer = pointer;
	}

	@Override
	public void apply(final IntConsumer jump, final Stack stack) {
		if (stack.pop() == 0) {
			jump.accept(this.pointer);
		}
	}
}
