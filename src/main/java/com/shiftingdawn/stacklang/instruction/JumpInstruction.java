package com.shiftingdawn.stacklang.instruction;

import com.shiftingdawn.stacklang.Instruction;
import com.shiftingdawn.stacklang.Stack;

import java.util.Objects;

public class JumpInstruction implements Instruction {

	private final int pointer;

	public JumpInstruction(final Integer pointer) {
		this.pointer = Objects.requireNonNull(pointer, "Missing jump pointer");
	}

	@Override
	public void apply(final Stack stack) {
		stack.push(this.pointer);
	}
}
