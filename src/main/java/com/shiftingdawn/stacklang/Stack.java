package com.shiftingdawn.stacklang;

public class Stack {

	public static final int DEFAULT_STACK_SIZE = 64;
	private int[] stack = new int[Stack.DEFAULT_STACK_SIZE];
	private int pointer = -1;

	private void ensureSize() {
		if (this.pointer + 1 == this.stack.length) {
			final int[] currentStack = this.stack;
			this.stack = new int[this.stack.length * 2];
			System.arraycopy(currentStack, 0, this.stack, 0, currentStack.length);
		}
	}

	public void push(final int x) {
		this.ensureSize();
		this.stack[++this.pointer] = x;
	}

	public void push(final short x) {
		this.push((int) x);
	}

	public void push(final byte x) {
		this.push((int) x);
	}

	public void push(final boolean x) {
		this.push(x ? 1 : 0);
	}

	public int pop() {
		if (this.pointer == -1) {
			throw new StackUnderflowError();
		}
		return this.stack[this.pointer--];
	}
}
