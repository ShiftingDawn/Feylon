package com.shiftingdawn.feylon.lang;

public enum Keywords {

	FUNCTION("function"),
	CONST("const"),

	END("end"),

	IF("if"),
	ELSE("else"),
	WHILE("while"),
	DO("do");

	public final String textValue;

	Keywords(final String textValue) {
		this.textValue = textValue;
	}

	public static Keywords getByText(final String str) {
		for (final Keywords keyword : Keywords.values()) {
			if (keyword.textValue.equals(str)) {
				return keyword;
			}
		}
		return null;
	}
}
