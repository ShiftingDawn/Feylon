package com.shiftingdawn.feylon.ins;

import com.shiftingdawn.feylon.Instruction;
import com.shiftingdawn.feylon.Memory;
import com.shiftingdawn.feylon.Stack;

import java.util.function.IntConsumer;

public class PushBooleanInstruction implements Instruction {

	private final boolean x;

	public PushBooleanInstruction(final boolean x) {
		this.x = x;
	}

	@Override
	public void apply(final IntConsumer jump, final Stack data, final Stack returnStack, final Memory memory) {
		data.push(this.x);
	}
}
