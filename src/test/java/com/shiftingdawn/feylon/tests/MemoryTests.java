package com.shiftingdawn.feylon.tests;

import com.shiftingdawn.feylon.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MemoryTests {

	private Memory memory;
	private int[] array;

	@Test
	public void testInit() {
		assertArrayEquals(new int[Memory.DEFAULT_MEMORY_SIZE], this.array);
	}

	@Test
	public void testMemSet() {
		this.memory.set(0, 10);
		assertEquals(10, this.array[0]);
		this.memory.set(1, 20);
		assertEquals(20, this.array[1]);
	}

	@Test
	public void testMemSetByte() {
		this.memory.set(0, (byte) 10);
		assertEquals(10, this.array[0]);
	}

	@Test
	public void testMemSetShort() {
		this.memory.set(0, (short) 10);
		assertEquals(10, this.array[0]);
	}

	@Test
	public void testMemSetBoolean() {
		this.memory.set(0, true);
		assertEquals(1, this.array[0]);
		this.memory.set(10, false);
		assertEquals(0, this.array[10]);
	}

	@Test
	public void testMemGet() {
		this.array[0] = 10;
		this.array[1] = 100;
		assertEquals(10, this.memory.get(0));
		assertEquals(100, this.memory.get(1));
	}

	@Test
	public void testInvalidPointers() {
		assertThrows(SegmentationError.class, () -> this.memory.set(-1, 0));
		assertThrows(SegmentationError.class, () -> this.memory.set(Memory.DEFAULT_MEMORY_SIZE, 0));
		assertThrows(SegmentationError.class, () -> this.memory.get(-1));
		assertThrows(SegmentationError.class, () -> this.memory.get(Memory.DEFAULT_MEMORY_SIZE));
	}

	@Test
	public void testMemSetInstruction() {
		final Instruction[] program = Parser.parse(List.of("0 1 memset 10 10 memset"));
		new Simulator(new Stack(), this.memory).execute(program);
		assertEquals(1, this.memory.get(0));
		assertEquals(10, this.memory.get(10));
	}

	@Test
	public void testMemGetInstruction() {
		this.array[0] = 1;
		this.array[10] = 10;
		final Instruction[] program = Parser.parse(List.of("0 memget 10 memget"));
		final Stack stack = new Stack();
		new Simulator(stack, this.memory).execute(program);
		assertEquals(10, stack.pop());
		assertEquals(1, stack.pop());
	}

	@BeforeEach
	public void init() throws NoSuchFieldException, IllegalAccessException {
		this.memory = new Memory();
		final Field arrayField = Memory.class.getDeclaredField("memory");
		arrayField.setAccessible(true);
		this.array = (int[]) arrayField.get(this.memory);
	}
}