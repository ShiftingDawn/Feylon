package com.shiftingdawn.stacklang.ops;

import com.shiftingdawn.stacklang.Op;
import com.shiftingdawn.stacklang.Stack;

public class SubtractOp implements Op {

	@Override
	public void apply(final Stack stack, final Object token) {
		final double a = stack.pop();
		final double b = stack.pop();
		stack.add(b - a);
	}
}
