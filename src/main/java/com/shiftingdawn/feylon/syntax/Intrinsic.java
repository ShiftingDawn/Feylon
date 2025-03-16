package com.shiftingdawn.feylon.syntax;

import java.util.Arrays;
import java.util.Optional;

public enum Intrinsic {

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
	GREATER_OR_EQUAL(">=");

	private final String textValue;

	Intrinsic(final String textValue) {
		this.textValue = textValue;
	}

	public static Optional<Intrinsic> getByText(final String str) {
		return Arrays.stream(Intrinsic.values())
				.filter(intrinsic -> intrinsic.textValue.equals(str))
				.findFirst();
	}
}