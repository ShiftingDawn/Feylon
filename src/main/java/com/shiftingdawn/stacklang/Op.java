package com.shiftingdawn.stacklang;

import java.util.Arrays;
import java.util.Optional;

public enum Op {

	OP_ADD("+", 2),
	OP_PRINT(".", 1);

	public final String symbol;
	public final int pops;

	Op(String symbol, int pops) {
		this.symbol = symbol;
		this.pops = pops;
	}

	public static Optional<Op> parse(String str) {
		return Arrays.stream(values()).filter(op -> op.symbol.equals(str)).findFirst();
	}
}