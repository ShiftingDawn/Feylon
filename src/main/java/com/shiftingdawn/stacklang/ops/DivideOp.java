package com.shiftingdawn.stacklang.ops;

import com.shiftingdawn.stacklang.Op;
import com.shiftingdawn.stacklang.Stack;

public class DivideOp implements Op {

	@Override
	public void apply(Stack stack) {
		double a = stack.pop();
		double b = stack.pop();
		stack.add(b / a);
	}
}
