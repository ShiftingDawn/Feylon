package com.shiftingdawn.stacklang;

public class Operator {

	public static void apply(Stack stack, Op op) {
		if (stack.size() < op.pops) {
			throw new StackUnderflowError();
		}
		switch (op) {
			case OP_ADD -> stack.push(stack.pop() + stack.pop());
			case OP_PRINT -> System.out.println(stack.pop());
			default -> throw new AssertionError("Missing definition for op: " + op);
		}
	}
}