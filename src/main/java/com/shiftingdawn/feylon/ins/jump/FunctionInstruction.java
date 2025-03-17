package com.shiftingdawn.feylon.ins.jump;

import com.shiftingdawn.feylon.Instruction;
import com.shiftingdawn.feylon.Memory;
import com.shiftingdawn.feylon.Stack;

import java.util.function.IntConsumer;

public class FunctionInstruction implements Instruction {

	private final int skipPointer;

	public FunctionInstruction(final int skipPointer) {
		this.skipPointer = skipPointer;
	}

	@Override
	public void apply(final IntConsumer jump, final Stack data, final Stack returnStack, final Memory memory) {
		jump.accept(this.skipPointer);
	}
}
