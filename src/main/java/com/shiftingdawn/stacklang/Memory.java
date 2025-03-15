package com.shiftingdawn.stacklang;

public class Memory {

	public static final int DEFAULT_MEMORY_SIZE = 640;
	private final int[] memory = new int[Memory.DEFAULT_MEMORY_SIZE];

	public void set(final int pointer, final int x) {
		if (pointer < 0 || pointer >= this.memory.length) {
			throw new SegmentationError();
		}
		this.memory[pointer] = x;
	}

	public void set(final int pointer, final short x) {
		this.set(pointer, (int) x);
	}

	public void set(final int pointer, final byte x) {
		this.set(pointer, (int) x);
	}

	public void set(final int pointer, final boolean x) {
		this.set(pointer, x ? 1 : 0);
	}

	public int get(final int pointer) {
		if (pointer < 0 || pointer >= this.memory.length) {
			throw new SegmentationError();
		}
		return this.memory[pointer];
	}
}
