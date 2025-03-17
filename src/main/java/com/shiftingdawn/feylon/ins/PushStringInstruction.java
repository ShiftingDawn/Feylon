package com.shiftingdawn.feylon.ins;

import com.shiftingdawn.feylon.Instruction;
import com.shiftingdawn.feylon.Memory;
import com.shiftingdawn.feylon.Stack;

import java.util.function.IntConsumer;

public class PushStringInstruction implements Instruction {

	private final String x;

	public PushStringInstruction(final String x) {
		this.x = x;
	}

	@Override
	public void apply(final IntConsumer jump, final Stack data, final Stack returnStack, final Memory memory) {
		final int pointer = memory.getNextStringPointer();
		final int writtenSize = memory.setString(pointer, this.x);
		data.push(writtenSize);
		data.push(pointer);
	}
}
