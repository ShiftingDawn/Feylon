package com.shiftingdawn.stacklang.ops;

import com.shiftingdawn.stacklang.Op;
import com.shiftingdawn.stacklang.Stack;

public class EqualsOp implements Op {

	@Override
	public void apply(final Stack stack, final Object token) {
		final double a = stack.pop();
		final double b = stack.pop();
		stack.push(a == b ? 1.0 : 0.0);
	}
}
