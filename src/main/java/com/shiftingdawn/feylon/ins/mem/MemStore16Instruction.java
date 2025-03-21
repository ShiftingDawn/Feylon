package com.shiftingdawn.feylon.ins.mem;

import com.shiftingdawn.feylon.Instruction;
import com.shiftingdawn.feylon.Memory;
import com.shiftingdawn.feylon.Stack;

import java.util.function.IntConsumer;

public class MemStore16Instruction implements Instruction {

	@Override
	public void apply(final IntConsumer jump, final Stack data, final Stack returnStack, final Memory memory) {
		final int ptr = data.pop();
		final int x = data.pop();
		memory.set(ptr, (byte) x);
		memory.set(ptr + 1, (byte) (x >> 8));
	}
}