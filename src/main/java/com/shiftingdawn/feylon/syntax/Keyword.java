package com.shiftingdawn.feylon.syntax;

import java.util.Arrays;
import java.util.Optional;

public enum Keyword {

	FUNCTION("function"),
	END("end"),
	IF("if"),
	ELSE("else"),
	WHILE("while"),
	DO("do");

	private final String textValue;

	Keyword(final String textValue) {
		this.textValue = textValue;
	}

	public static Optional<Keyword> getByText(final String str) {
		return Arrays.stream(Keyword.values())
				.filter(keyword -> keyword.textValue.equals(str))
				.findFirst();
	}
}