package com.shiftingdawn.feylon.ins.mem;

import com.shiftingdawn.feylon.Instruction;
import com.shiftingdawn.feylon.Memory;
import com.shiftingdawn.feylon.Stack;

import java.util.function.IntConsumer;

public class MemSetInstruction implements Instruction {

	@Override
	public void apply(final IntConsumer jump, final Stack data, final Stack returnStack, final Memory memory) {
		final int value = data.pop();
		final int pointer = data.pop();
		memory.setInt(pointer, value);
	}
}
