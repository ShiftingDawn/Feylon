package com.shiftingdawn.stacklang;

import com.shiftingdawn.stacklang.ops.*;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Supplier;

public enum Ops {

	OP_PUSH(null, 0, PushOp::new),
	OP_PRINT(".", 1, PrintOp::new),
	OP_EQUALS("=", 2, EqualsOp::new),
	OP_ADD("+", 2, AddOp::new),
	OP_SUBTRACT("-", 2, SubtractOp::new),
	OP_MULTIPLY("*", 2, MultiplyOp::new),
	OP_DIVIDE("/", 2, DivideOp::new);

	private static final Ops[] OPS = Arrays.stream(Ops.values())
			.filter(op -> op != Ops.OP_PUSH)
			.toArray(Ops[]::new);
	private final String symbol;
	private final int pops;
	private final Supplier<? extends Op> factory;

	Ops(final String symbol, final int pops, final Supplier<? extends Op> factory) {
		this.symbol = symbol;
		this.pops = pops;
		this.factory = factory;
	}

	public void apply(final Stack stack, final Object token) {
		if (stack.size() < this.pops) {
			throw new StackUnderflowError();
		}
		this.factory.get().apply(stack, token);
	}

	public static Optional<Ops> parse(final String str) {
		return Arrays.stream(Ops.OPS).filter(op -> op.symbol.equals(str)).findFirst();
	}
}