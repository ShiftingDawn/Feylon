package com.shiftingdawn.feylon;

import java.util.Arrays;
import java.util.Optional;

public enum Ops {

	NOOP,
	OP_PUSH_INT,
	OP_PUSH_STRING,
	OP_POP("pop"),
	OP_DUP("dup"),
	OP_MEM("mem"),
	OP_MEM_SET("memset"),
	OP_MEM_GET("memget"),
	OP_PRINT("print"),
	OP_EQUALS('='),
	OP_NOT_EQUALS("!="),
	OP_LESS('<'),
	OP_GREATER('>'),
	OP_LESS_EQUAL("<="),
	OP_GREATER_EQUAL(">="),
	OP_ADD('+'),
	OP_SUBTRACT('-'),
	OP_MULTIPLY('*'),
	OP_DIVIDE('/'),
	OP_MOD('%'),
	OP_END("end"),
	OP_IF("if"),
	OP_ELSE("else"),
	OP_WHILE("while"),
	OP_DO("do");

	private static final Ops[] OPS_WITH_SYMBOL = Arrays.stream(Ops.values())
			.filter(op -> op.symbol != null)
			.toArray(Ops[]::new);
	private final String symbol;

	Ops(final String symbol) {
		this.symbol = symbol;
	}

	Ops(final char symbol) {
		this(String.valueOf(symbol));
	}

	Ops() {
		this(null);
	}

	public static Optional<Ops> getBySymbol(final String symbol) {
		return Arrays.stream(Ops.OPS_WITH_SYMBOL).filter(op -> op.symbol.equals(symbol)).findFirst();
	}
}