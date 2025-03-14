package com.shiftingdawn.stacklang;

public final class Main {

	public static void main(String[] args) {
		final Stack stack = new Stack();
		stack.add(1.0);
		stack.add(2.0);
		Operator.apply(stack, Op.OP_ADD);
		Operator.apply(stack, Op.OP_PRINT);
	}
}