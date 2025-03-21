package com.shiftingdawn.feylon.lang;

public record LexedToken(TokenPos pos, String content) {

	public LexedToken(final String file, final int line, final int col, final String content) {
		this(new TokenPos(file, line, col), content);
	}
}