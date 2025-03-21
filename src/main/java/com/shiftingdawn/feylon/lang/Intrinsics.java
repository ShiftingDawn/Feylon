package com.shiftingdawn.feylon.lang;

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

	CAST_INTEGER("cast(int)", false),
	CAST_BOOLEAN("cast(bool)", false),
	CAST_STRING("cast(str)", false),
	CAST_POINTER("cast(ptr)", false),

	DUMP("dump"),
	POP("pop"),
	DUP("dup"),
	SWAP("swap"),
	OVER("over"),
	ROT("rot");

	public final String textValue;
	public final boolean hasInstruction;

	Intrinsics(final String textValue, final boolean hasInstruction) {
		this.textValue = textValue;
		this.hasInstruction = hasInstruction;
	}

	Intrinsics(final String textValue) {
		this(textValue, true);
	}

	public static Intrinsics getByText(final String str) {
		for (final Intrinsics intrinsic : Intrinsics.values()) {
			if (intrinsic.textValue.equals(str)) {
				return intrinsic;
			}
		}
		return null;
	}
}