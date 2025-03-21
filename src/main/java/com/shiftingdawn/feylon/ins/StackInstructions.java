package com.shiftingdawn.feylon.ins;

import com.shiftingdawn.feylon.Memory;
import com.shiftingdawn.feylon.Stack;

import java.util.function.IntConsumer;

public class StackInstructions {

	public static Instruction push(final int value) {
		return (jump, data, returnStack, memory) -> data.push(value);
	}

	public static Instruction push(final String value) {
		return (jump, data, returnStack, memory) -> {
			final int pointer = memory.getNextStringPointer();
			final int writtenSize = memory.setString(pointer, value);
			data.push(writtenSize);
			data.push(pointer);
		};
	}

	public static void dump(final IntConsumer jump, final Stack data, final Stack returnStack, final Memory memory) {
		System.out.println(data.pop());
	}

	public static void pop(final IntConsumer jump, final Stack data, final Stack returnStack, final Memory memory) {
		data.pop();
	}

	public static void dup(final IntConsumer jump, final Stack data, final Stack returnStack, final Memory memory) {
		final int x = data.pop();
		data.push(x);
		data.push(x);
	}

	public static void swap(final IntConsumer jump, final Stack data, final Stack returnStack, final Memory memory) {
		final int a = data.pop();
		final int b = data.pop();
		data.push(a);
		data.push(b);
	}

	public static void over(final IntConsumer jump, final Stack data, final Stack returnStack, final Memory memory) {
		final int a = data.pop();
		final int b = data.pop();
		data.push(b);
		data.push(a);
		data.push(b);
	}

	public static void rot(final IntConsumer jump, final Stack data, final Stack returnStack, final Memory memory) {
		final int c = data.pop();
		final int b = data.pop();
		final int a = data.pop();
		data.push(b);
		data.push(c);
		data.push(a);
	}
}