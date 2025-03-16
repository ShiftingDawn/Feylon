package com.shiftingdawn.feylon.ins.jump;

import com.shiftingdawn.feylon.JumpInstruction;
import com.shiftingdawn.feylon.Stack;

import java.util.function.IntConsumer;

public class ElseInstruction implements JumpInstruction {

	private final int pointer;

	public ElseInstruction(final int pointer) {
		this.pointer = pointer;
	}

	@Override
	public void apply(final IntConsumer jump, final Stack stack) {
		jump.accept(this.pointer);
	}
}
