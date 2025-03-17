package com.shiftingdawn.feylon.ins;

import com.shiftingdawn.feylon.Instruction;
import com.shiftingdawn.feylon.Memory;
import com.shiftingdawn.feylon.Stack;

import java.util.function.IntConsumer;

public class DupInstruction implements Instruction {

	@Override
	public void apply(final IntConsumer jump, final Stack data, final Stack returnStack, final Memory memory) {
		final int x = data.pop();
		data.push(x);
		data.push(x);
	}
}
