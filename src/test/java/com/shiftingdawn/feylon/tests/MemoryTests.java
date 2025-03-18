package com.shiftingdawn.feylon.tests;

import com.shiftingdawn.feylon.Memory;
import com.shiftingdawn.feylon.SegmentationError;
import com.shiftingdawn.feylon.Simulator;
import com.shiftingdawn.feylon.Stack;
import com.shiftingdawn.feylon.syntax.Compiler;
import com.shiftingdawn.feylon.syntax.Program;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MemoryTests {

	private Memory memory;
	private ByteBuffer array;

	@Test
	public void testInit() {
		final byte[] bufferArray = new byte[this.array.capacity()];
		this.array.get(bufferArray, 0, bufferArray.length);
		assertArrayEquals(new byte[Memory.DEFAULT_MEMORY_SIZE + Memory.STRING_MEMORY_SIZE], bufferArray);
	}

	@Test
	public void testMemSet() {
		this.memory.set(0, (byte) 10);
		assertEquals(10, this.array.get(0));
		this.memory.set(1, (byte) 20);
		assertEquals(20, this.array.get(1));
	}

	@Test
	public void testMemSetInt() {
		this.memory.setInt(0, 10);
		assertEquals(10, this.array.getInt(0));
		this.memory.setInt(1, 20);
		assertEquals(20, this.array.getInt(1));
	}

	@Test
	public void testMemSetBoolean() {
		this.memory.set(0, true);
		assertEquals(1, this.array.get(0));
		this.memory.set(10, false);
		assertEquals(0, this.array.get(10));
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
		assertThrows(SegmentationError.class, () -> this.memory.set(Memory.DEFAULT_MEMORY_SIZE + Memory.STRING_MEMORY_SIZE, (byte) 0));
		assertThrows(SegmentationError.class, () -> this.memory.get(-1));
		assertThrows(SegmentationError.class, () -> this.memory.get(Memory.DEFAULT_MEMORY_SIZE + Memory.STRING_MEMORY_SIZE));
	}

	@Test
	public void testMemSetInstruction() {
		final Program program = Compiler.compile("<generated>", List.of("0 cast-ptr 1 memset 10 cast-ptr 10 memset"));
		new Simulator(new Stack(), new Stack(), this.memory).execute(program);
		assertEquals(1, this.memory.getInt(0));
		assertEquals(10, this.memory.getInt(10));
	}

	@Test
	public void testMemGetInstruction() {
		this.array.put(0, (byte) 1);
		this.array.put(10, (byte) 10);
		final Program program = Compiler.compile("<generated>", List.of("0 cast-ptr memget 10 cast-ptr memget"), 2);
		final Stack stack = new Stack();
		new Simulator(stack, new Stack(), this.memory).execute(program);
		assertEquals(10, stack.pop());
		assertEquals(1, stack.pop());
	}

	@BeforeEach
	public void init() throws NoSuchFieldException, IllegalAccessException {
		this.memory = new Memory();
		final Field arrayField = Memory.class.getDeclaredField("memory");
		arrayField.setAccessible(true);
		this.array = (ByteBuffer) arrayField.get(this.memory);
	}
}