package com.shiftingdawn.feylon.lang;

public record RawToken(TokenPos pos, RawTokenType type, String txt, Object operand) {

	public RawToken {
		if (txt.trim().isBlank()) {
			throw new IllegalArgumentException("Token text cannot be empty");
		}
	}
}