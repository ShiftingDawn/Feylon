package com.shiftingdawn.feylon.lang;

public record TokenPos(String file, int lineNr, int col) {

	@Override
	public String toString() {
		return "%s:%d:%d".formatted(this.file, this.lineNr + 1, this.col + 1);
	}
}
