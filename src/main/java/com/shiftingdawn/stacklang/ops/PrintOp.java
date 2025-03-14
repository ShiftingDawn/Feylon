package com.shiftingdawn.stacklang.ops;

import com.shiftingdawn.stacklang.Op;
import com.shiftingdawn.stacklang.Stack;

public class PrintOp implements Op {

	@Override
	public void apply(Stack stack) {
		System.out.println(stack.pop());
	}
}
