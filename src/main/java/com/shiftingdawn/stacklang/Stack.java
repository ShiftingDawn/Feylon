package com.shiftingdawn.stacklang;

public class Stack {

	private int[] stack = new int[64];
	private int pointer = -1;

	private void ensureSize() {
		if (this.pointer == this.stack.length) {
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
