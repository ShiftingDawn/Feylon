package com.shiftingdawn.feylon.lang;

import java.io.Serial;

public class FeylonException extends RuntimeException {

	@Serial
	private static final long serialVersionUID = -9188584671879490814L;

	public FeylonException(final TokenPos pos, final String msg) {
		super("%s:%d:%d: ERROR: %s".formatted(pos.file(), pos.line() + 1, pos.pos() + 1, msg));
	}
}