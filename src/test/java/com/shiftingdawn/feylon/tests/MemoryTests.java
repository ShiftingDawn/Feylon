package com.shiftingdawn.feylon.tests;

import com.shiftingdawn.feylon.Memory;
import com.shiftingdawn.feylon.SegmentationError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;

public class MemoryTests {

	private Memory memory;
	private ByteBuffer array;

	@Test
	public void testInit() {
		final byte[] bufferArray = new byte[this.array.capacity()];
		this.array.get(bufferArray, 0, bufferArray.length);
		assertArrayEquals(new byte[64 + Memory.STRING_MEMORY_SIZE], bufferArray);
	}

	@Test
	public void testMemSet() {
		this.memory.set(0, (byte) 10);
		assertEquals(10, this.array.get(0));
		this.memory.set(1, (byte) 20);
		assertEquals(20, this.array.get(1));
	}

	@Test
	public void testMemGet() {
		this.array.put(0, (byte) 10);
		this.array.put(1, (byte) 100);
		assertEquals(10, this.memory.get(0));
		assertEquals(100, this.memory.get(1));
	}

	@Test
	public void testInvalidPointers() {
		assertThrows(SegmentationError.class, () -> this.memory.set(-1, (byte) 0));
		assertThrows(SegmentationError.class, () -> this.memory.set(64 + Memory.STRING_MEMORY_SIZE, (byte) 0));
		assertThrows(SegmentationError.class, () -> this.memory.get(-1));
		assertThrows(SegmentationError.class, () -> this.memory.get(64 + Memory.STRING_MEMORY_SIZE));
	}

	@BeforeEach
	public void init() throws NoSuchFieldException, IllegalAccessException {
		this.memory = new Memory(64);
		final Field arrayField = Memory.class.getDeclaredField("memory");
		arrayField.setAccessible(true);
		this.array = (ByteBuffer) arrayField.get(this.memory);
	}
}