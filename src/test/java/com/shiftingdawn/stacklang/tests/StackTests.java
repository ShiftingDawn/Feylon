package com.shiftingdawn.stacklang.tests;

import com.shiftingdawn.stacklang.Stack;
import com.shiftingdawn.stacklang.StackUnderflowError;
import com.shiftingdawn.stacklang.ins.DupInstruction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

public class StackTests {

	private Stack stack;
	private Supplier<int[]> array;
	private IntSupplier pointer;

	@Test
	public void testInit() {
		assertArrayEquals(new int[Stack.DEFAULT_STACK_SIZE], this.array.get());
		assertEquals(-1, this.pointer.getAsInt());
	}

	@Test
	public void testPush() {
		this.stack.push(1);
		assertEquals(1, this.array.get()[0]);
		assertEquals(0, this.pointer.getAsInt());
	}

	@Test
	public void testPush2() {
		this.stack.push(1);
		assertEquals(1, this.array.get()[0]);
		assertEquals(0, this.pointer.getAsInt());
		this.stack.push(2);
		assertEquals(2, this.array.get()[1]);
		assertEquals(1, this.pointer.getAsInt());
	}

	@Test
	public void testPushByte() {
		this.stack.push((byte) 1);
		assertEquals(1, this.array.get()[0]);
		assertEquals(0, this.pointer.getAsInt());
	}

	@Test
	public void testPushShort() {
		this.stack.push((short) 1);
		assertEquals(1, this.array.get()[0]);
		assertEquals(0, this.pointer.getAsInt());
	}

	@Test
	public void testPushBoolean() {
		this.stack.push(false);
		assertEquals(0, this.array.get()[0]);
		assertEquals(0, this.pointer.getAsInt());
		this.stack.push(true);
		assertEquals(1, this.array.get()[1]);
		assertEquals(1, this.pointer.getAsInt());
	}

	@Test
	public void testPushResize() {
		for (int i = 0; i < Stack.DEFAULT_STACK_SIZE - 1; ++i) {
			this.stack.push(1);
		}
		assertEquals(Stack.DEFAULT_STACK_SIZE, this.array.get().length);
		this.stack.push(1);
		assertEquals(Stack.DEFAULT_STACK_SIZE, this.array.get().length);
		this.stack.push(1);
		assertEquals(Stack.DEFAULT_STACK_SIZE * 2, this.array.get().length);
	}

	@Test
	public void testPop() {
		this.stack.push(1);
		assertEquals(1, this.stack.pop());
		assertEquals(-1, this.pointer.getAsInt());
	}

	@Test
	public void testPopEmpty() {
		assertThrows(StackUnderflowError.class, this.stack::pop);
	}

	@Test
	public void testDup() {
		assertEquals(-1, this.pointer.getAsInt());
		this.stack.push(1);
		assertEquals(0, this.pointer.getAsInt());
		new DupInstruction().apply(this.stack);
		assertEquals(1, this.pointer.getAsInt());
		assertEquals(1, this.array.get()[1]);
	}

	@BeforeEach
	public void init() throws NoSuchFieldException {
		this.stack = new Stack();
		final Field arrayField = Stack.class.getDeclaredField("stack");
		arrayField.setAccessible(true);
		this.array = () -> {
			try {
				return (int[]) arrayField.get(StackTests.this.stack);
			} catch (final IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		};
		final Field pointerField = Stack.class.getDeclaredField("pointer");
		pointerField.setAccessible(true);
		this.pointer = () -> {
			try {
				return pointerField.getInt(this.stack);
			} catch (final IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		};
	}
}