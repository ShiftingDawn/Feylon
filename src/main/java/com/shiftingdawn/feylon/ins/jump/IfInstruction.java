package com.shiftingdawn.feylon.ins.jump;

import com.shiftingdawn.feylon.Instruction;
import com.shiftingdawn.feylon.Memory;
import com.shiftingdawn.feylon.Stack;

import java.util.function.IntConsumer;

public class IfInstruction implements Instruction {

	private final int pointer;

	public IfInstruction(final int pointer) {
		this.pointer = pointer;
	}

	@Override
	public void apply(final IntConsumer jump, final Stack data, final Stack returnStack, final Memory memory) {
		if (data.pop() == 0) {
			jump.accept(this.pointer);
		}
	}
}
