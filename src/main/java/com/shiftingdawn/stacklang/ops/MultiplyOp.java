package com.shiftingdawn.stacklang.ops;

import com.shiftingdawn.stacklang.Op;
import com.shiftingdawn.stacklang.Stack;

public class MultiplyOp implements Op {

	@Override
	public void apply(final Stack stack, final Object token) {
		stack.add(stack.pop() * stack.pop());
	}
}
