package com.shiftingdawn.feylon.ins;

import com.shiftingdawn.feylon.Memory;
import com.shiftingdawn.feylon.Stack;

import java.util.function.IntConsumer;

public class ControlFlowInstructions {

	public static Instruction jump(final int pointer) {
		return (final IntConsumer jump, final Stack data, final Stack returnStack, final Memory memory) -> jump.accept(pointer);
	}

	public static Instruction jumpEq(final int pointer) {
		return (final IntConsumer jump, final Stack data, final Stack returnStack, final Memory memory) -> {
			if (data.pop() == 1) {
				jump.accept(pointer);
			}
		};
	}

	public static Instruction jumpNeq(final int pointer) {
		return (final IntConsumer jump, final Stack data, final Stack returnStack, final Memory memory) -> {
			if (data.pop() == 0) {
				jump.accept(pointer);
			}
		};
	}

	public static Instruction call(final int callPointer, final int returnPointer) {
		return (jump, data, returnStack, memory) -> {
			returnStack.push(returnPointer);
			jump.accept(callPointer);
		};
	}

	public static void ret(final IntConsumer jump, final Stack data, final Stack returnStack, final Memory memory) {
		jump.accept(returnStack.pop());
	}
}