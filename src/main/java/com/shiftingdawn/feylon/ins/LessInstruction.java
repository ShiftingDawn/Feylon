package com.shiftingdawn.feylon.ins;

import com.shiftingdawn.feylon.Instruction;
import com.shiftingdawn.feylon.Memory;
import com.shiftingdawn.feylon.Stack;

import java.util.function.IntConsumer;

public class LessInstruction implements Instruction {

	@Override
	public void apply(final IntConsumer jump, final Stack data, final Stack returnStack, final Memory memory) {
		final double a = data.pop();
		final double b = data.pop();
		data.push(b < a);
	}
}
