package com.shiftingdawn.feylon.ins.jump;

import com.shiftingdawn.feylon.Instruction;
import com.shiftingdawn.feylon.Memory;
import com.shiftingdawn.feylon.Stack;
import com.shiftingdawn.feylon.syntax.FunctionDef;

import java.util.function.IntConsumer;

public class CallFunctionInstruction implements Instruction {

	private final FunctionDef functionDef;
	private final int returnPointer;

	public CallFunctionInstruction(final FunctionDef functionDef, final int returnPointer) {
		this.functionDef = functionDef;
		this.returnPointer = returnPointer;
	}

	@Override
	public void apply(final IntConsumer jump, final Stack data, final Stack returnStack, final Memory memory) {
		returnStack.push(this.returnPointer);
		jump.accept(this.functionDef.pointer());
	}
}
