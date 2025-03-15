package com.shiftingdawn.stacklang.ops;

import com.shiftingdawn.stacklang.Op;
import com.shiftingdawn.stacklang.Stack;

public class PushOp implements Op {

	@Override
	public void apply(final Stack stack, final Object token) {
		stack.push((double) token);
	}
}
