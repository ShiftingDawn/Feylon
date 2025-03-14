package com.shiftingdawn.stacklang;

import com.shiftingdawn.stacklang.ops.*;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Supplier;

public enum Ops {

	OP_PRINT(".", 1, PrintOp::new),
	OP_ADD("+", 2, AddOp::new),
	OP_SUBTRACT("-", 2, SubtractOp::new),
	OP_MULTIPLY("*", 2, MultiplyOp::new),
	OP_DIVIDE("/", 2, DivideOp::new);

	private final String symbol;
	private final int pops;
	private final Supplier<? extends Op> factory;

	Ops(String symbol, int pops, Supplier<? extends Op> factory) {
		this.symbol = symbol;
		this.pops = pops;
		this.factory = factory;
	}

	public void apply(Stack stack) {
		if (stack.size() < this.pops) {
			throw new StackUnderflowError();
		}
		this.factory.get().apply(stack);
	}

	public static Optional<Ops> parse(String str) {
		return Arrays.stream(values()).filter(op -> op.symbol.equals(str)).findFirst();
	}
}