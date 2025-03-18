package com.shiftingdawn.feylon.syntax;

public record TokenPos(String file, int lineNr, int col) {

	@Override
	public String toString() {
		return "%s:%d:%d".formatted(this.file, this.lineNr, this.col);
	}
}
