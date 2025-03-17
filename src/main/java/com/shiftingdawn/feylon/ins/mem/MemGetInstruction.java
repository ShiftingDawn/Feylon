package com.shiftingdawn.feylon.ins.mem;

import com.shiftingdawn.feylon.Instruction;
import com.shiftingdawn.feylon.Memory;
import com.shiftingdawn.feylon.Stack;

import java.util.function.IntConsumer;

public class MemGetInstruction implements Instruction {

	@Override
	public void apply(final IntConsumer jump, final Stack data, final Stack returnStack, final Memory memory) {
		final int pointer = data.pop();
		final int value = memory.get(pointer);
		data.push(value);
	}
}
