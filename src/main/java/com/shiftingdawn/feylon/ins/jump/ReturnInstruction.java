package com.shiftingdawn.feylon.ins.jump;

import com.shiftingdawn.feylon.Instruction;
import com.shiftingdawn.feylon.Memory;
import com.shiftingdawn.feylon.Stack;

import java.util.function.IntConsumer;

public class ReturnInstruction implements Instruction {

	@Override
	public void apply(IntConsumer jump, Stack data, Stack returnStack, Memory memory) {
		jump.accept(returnStack.pop());
	}
}