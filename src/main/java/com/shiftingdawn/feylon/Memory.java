package com.shiftingdawn.feylon;

import java.nio.ByteBuffer;

public class Memory {

	public static final int DEFAULT_MEMORY_SIZE = 64_000;
	public static final int STRING_MEMORY_SIZE = 64_000;
	private final ByteBuffer memory = ByteBuffer.allocateDirect(Memory.DEFAULT_MEMORY_SIZE + Memory.STRING_MEMORY_SIZE);

	public void set(final int pointer, final byte x) {
		if (pointer < 0 || pointer >= this.memory.capacity()) {
			throw new SegmentationError();
		}
		this.memory.put(pointer, x);
	}

	public void set(final int pointer, final boolean x) {
		this.set(pointer, x ? (byte) 1 : (byte) 0);
	}

	public void setInt(final int pointer, final int x) {
		if (pointer < 0 || pointer >= this.memory.capacity() - 3) {
			throw new SegmentationError();
		}
		this.memory.putInt(pointer, x);
	}

	public byte get(final int pointer) {
		if (pointer < 0 || pointer >= this.memory.capacity()) {
			throw new SegmentationError();
		}
		return this.memory.get(pointer);
	}

	public int getInt(final int pointer) {
		if (pointer < 0 || pointer >= this.memory.capacity() - 3) {
			throw new SegmentationError();
		}
		return this.memory.getInt(pointer);
	}
}
