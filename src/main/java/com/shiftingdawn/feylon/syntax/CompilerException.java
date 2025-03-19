package com.shiftingdawn.feylon.syntax;

import java.io.Serial;

import com.shiftingdawn.feylon.lang.TokenPos;

public class CompilerException extends AssertionError {

	@Serial
	private static final long serialVersionUID = -6578738322980705074L;

	public CompilerException(final TokenPos pos, final String message) {
		super("%s: ERROR: %s".formatted(pos, message));
	}
}