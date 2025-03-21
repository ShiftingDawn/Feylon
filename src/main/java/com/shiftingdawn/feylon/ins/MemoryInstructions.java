package com.shiftingdawn.feylon.ins;

import com.shiftingdawn.feylon.Memory;
import com.shiftingdawn.feylon.Stack;

import java.util.function.IntConsumer;

public class MemoryInstructions {

	public static void store8(final IntConsumer jump, final Stack data, final Stack returnStack, final Memory memory) {
		final int ptr = data.pop();
		final int x = data.pop();
		memory.set(ptr, (byte) x);
	}

	public static void store16(final IntConsumer jump, final Stack data, final Stack returnStack, final Memory memory) {
		final int ptr = data.pop();
		final int x = data.pop();
		memory.set(ptr, (byte) x);
		memory.set(ptr + 1, (byte) (x >> 8));
	}

	public static void store32(final IntConsumer jump, final Stack data, final Stack returnStack, final Memory memory) {
		final int ptr = data.pop();
		final int x = data.pop();
		memory.set(ptr, (byte) x);
		memory.set(ptr + 1, (byte) (x >> 8));
		memory.set(ptr + 2, (byte) (x >> 16));
		memory.set(ptr + 3, (byte) (x >> 25));
	}

	public static void load8(final IntConsumer jump, final Stack data, final Stack returnStack, final Memory memory) {
		final int ptr = data.pop();
		data.push(memory.get(ptr));
	}

	public static void load16(final IntConsumer jump, final Stack data, final Stack returnStack, final Memory memory) {
		final int ptr = data.pop();
		int x = memory.get(ptr) & 0xFF;
		x |= (memory.get(ptr + 1) & 0xFF) << 8;
		data.push(x);
	}

	public static void load32(final IntConsumer jump, final Stack data, final Stack returnStack, final Memory memory) {
		final int ptr = data.pop();
		int x = memory.get(ptr) & 0xFF;
		x |= (memory.get(ptr + 1) & 0xFF) << 8;
		x |= (memory.get(ptr + 2) & 0xFF) << 16;
		x |= (memory.get(ptr + 3) & 0xFF) << 24;
		data.push(x);
	}
}
