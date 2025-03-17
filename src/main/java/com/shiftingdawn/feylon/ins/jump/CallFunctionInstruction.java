package com.shiftingdawn.feylon.ins.jump;

import com.shiftingdawn.feylon.Instruction;
import com.shiftingdawn.feylon.Memory;
import com.shiftingdawn.feylon.Stack;

import java.util.function.IntConsumer;

public class CallFunctionInstruction implements Instruction {

	private final int functionPointer;
	private final int returnPointer;

	public CallFunctionInstruction(final int functionPointer, final int returnPointer) {
		this.functionPointer = functionPointer;
		this.returnPointer = returnPointer;
	}

	@Override
	public void apply(final IntConsumer jump, final Stack data, final Stack returnStack, final Memory memory) {
		returnStack.push(this.returnPointer);
		jump.accept(this.functionPointer);
	}
}
