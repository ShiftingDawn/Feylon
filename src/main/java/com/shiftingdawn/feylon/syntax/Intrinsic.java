package com.shiftingdawn.feylon.syntax;

import java.util.Arrays;
import java.util.Optional;

public enum Intrinsic {

	TRUE("true"),
	FALSE("false"),

	ADD("+"),
	SUBTRACT("-"),
	MULTIPLY("*"),
	DIVIDE("/"),
	MODULO("%"),

	EQUALS("="),
	NOT_EQUALS("!="),
	LESS("<"),
	GREATER(">"),
	LESS_OR_EQUAL("<="),
	GREATER_OR_EQUAL(">="),

	SHIFT_LEFT("<<"),
	SHIFT_RIGHT(">>"),
	BITWISE_AND("&"),
	BITWISE_OR("|"),
	BITWISE_XOR("^"),

	CAST_INTEGER("cast-int"),
	CAST_BOOLEAN("cast-bool"),
	CAST_POINTER("cast-ptr");

	public final String textValue;

	Intrinsic(final String textValue) {
		this.textValue = textValue;
	}

	public static Optional<Intrinsic> getByText(final String str) {
		return Arrays.stream(Intrinsic.values())
				.filter(intrinsic -> intrinsic.textValue.equals(str))
				.findFirst();
	}
}