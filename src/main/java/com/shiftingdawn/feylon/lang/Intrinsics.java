package com.shiftingdawn.feylon.lang;

import java.util.Arrays;
import java.util.Optional;

public enum Intrinsics {

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

	ARROW("->", false),
	CAST_INTEGER("cast(int)", false),
	CAST_BOOLEAN("cast(bool)", false),
	CAST_STRING("cast(str)", false),
	CAST_POINTER("cast(ptr)", false);

	public final String textValue;
	public final boolean hasInstruction;

	Intrinsics(final String textValue, final boolean hasInstruction) {
		this.textValue = textValue;
		this.hasInstruction = hasInstruction;
	}

	Intrinsics(final String textValue) {
		this(textValue, true);
	}

	public static Optional<Intrinsics> getByText(final String str) {
		return Arrays.stream(Intrinsics.values())
				.filter(intrinsic -> intrinsic.textValue.equals(str))
				.findFirst();
	}
}