package com.shiftingdawn.stacklang.ops;

import com.shiftingdawn.stacklang.Op;
import com.shiftingdawn.stacklang.Stack;

public class MultiplyOp implements Op {

	@Override
	public void apply(Stack stack) {
		stack.add(stack.pop() * stack.pop());
	}
}
