package com.shiftingdawn.feylon;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class Memory {

	public static final int DEFAULT_MEMORY_SIZE = 64_000;
	public static final int STRING_MEMORY_SIZE = 64_000;
	private static final int MEMORY_LIMIT = Memory.DEFAULT_MEMORY_SIZE + Memory.STRING_MEMORY_SIZE;
	private final ByteBuffer memory = ByteBuffer.allocateDirect(Memory.MEMORY_LIMIT);
	private int nextStringPointer = 0;

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

	public int setString(final int pointer, final String str) {
		if (pointer < 0 || pointer >= Memory.STRING_MEMORY_SIZE) {
			throw new SegmentationError();
		}
		final byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
		this.memory.put(Memory.MEMORY_LIMIT - Memory.STRING_MEMORY_SIZE + pointer, bytes);
		this.nextStringPointer += bytes.length;
		return bytes.length;
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

	public String getString(final int pointer, final int size) {
		if (pointer < 0 || pointer >= Memory.STRING_MEMORY_SIZE || size >= Memory.STRING_MEMORY_SIZE || pointer + size >= Memory.STRING_MEMORY_SIZE) {
			throw new SegmentationError();
		}
		final byte[] bytes = new byte[size];
		this.memory.get(Memory.MEMORY_LIMIT - Memory.STRING_MEMORY_SIZE + pointer, bytes);
		return new String(bytes, StandardCharsets.UTF_8);
	}

	public int getNextStringPointer() {
		return this.nextStringPointer;
	}
}
