package com.shiftingdawn.feylon.ins.jump;

import com.shiftingdawn.feylon.JumpInstruction;
import com.shiftingdawn.feylon.Stack;

import java.util.function.IntConsumer;

public class EndInstruction implements JumpInstruction {

	private final int pointer;

	public EndInstruction(final int pointer) {
		this.pointer = pointer;
	}

	@Override
	public void apply(final IntConsumer jump, final Stack stack) {
		jump.accept(this.pointer);
	}
}
