package com.shiftingdawn.feylon.ins.jump;

import com.shiftingdawn.feylon.Instruction;
import com.shiftingdawn.feylon.Memory;
import com.shiftingdawn.feylon.Stack;

import java.util.function.IntConsumer;

public class ElseInstruction implements Instruction {

	private final int pointer;

	public ElseInstruction(final int pointer) {
		this.pointer = pointer;
	}

	@Override
	public void apply(final IntConsumer jump, final Stack data, final Stack returnStack, final Memory memory) {
		jump.accept(this.pointer);
	}
}
