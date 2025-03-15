package com.shiftingdawn.stacklang.instruction;

import com.shiftingdawn.stacklang.JumpInstruction;
import com.shiftingdawn.stacklang.Stack;

import java.util.function.IntConsumer;

public class IfInstruction implements JumpInstruction {

	private final int pointer;

	public IfInstruction(final int pointer) {
		this.pointer = pointer;
	}

	@Override
	public void apply(final IntConsumer jump, final Stack stack) {
		if (stack.pop() == 0) {
			jump.accept(this.pointer);
		}
	}
}
