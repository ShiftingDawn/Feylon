package com.shiftingdawn.feylon.ins.mem;

import com.shiftingdawn.feylon.Instruction;
import com.shiftingdawn.feylon.Memory;
import com.shiftingdawn.feylon.Stack;

import java.util.function.IntConsumer;

public class MemLoad16Instruction implements Instruction {

	@Override
	public void apply(final IntConsumer jump, final Stack data, final Stack returnStack, final Memory memory) {
		final int ptr = data.pop();
		int x = memory.get(ptr) & 0xFF;
		x |= (memory.get(ptr + 1) & 0xFF) << 8;
		data.push(x);
	}
}