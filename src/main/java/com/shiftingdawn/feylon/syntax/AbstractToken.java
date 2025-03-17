package com.shiftingdawn.feylon.syntax;

public abstract class AbstractToken<T> {

	public final Location location;
	public final Tokenizer.TokenType type;
	public final T value;

	protected AbstractToken(final Location location, final Tokenizer.TokenType type, final T value) {
		this.location = location;
		this.type = type;
		this.value = value;
	}

	public static class Int extends AbstractToken<Integer> {

		public Int(final Location location, final int value) {
			super(location, Tokenizer.TokenType.INT, value);
		}
	}

	public static class String extends AbstractToken<java.lang.String> {

		public String(final Location location, final java.lang.String value) {
			super(location, Tokenizer.TokenType.STRING, value);
		}
	}

	public static class Keyword extends AbstractToken<com.shiftingdawn.feylon.syntax.Keyword> {

		public Keyword(final Location location, final com.shiftingdawn.feylon.syntax.Keyword value) {
			super(location, Tokenizer.TokenType.KEYWORD, value);
		}
	}

	public static class Intrinsic extends AbstractToken<com.shiftingdawn.feylon.syntax.Intrinsic> {

		public Intrinsic(final Location location, final com.shiftingdawn.feylon.syntax.Intrinsic value) {
			super(location, Tokenizer.TokenType.INTRINSIC, value);
		}
	}

	public static class Instruction extends AbstractToken<java.lang.String> {

		public Instruction(final Location location, final java.lang.String value) {
			super(location, Tokenizer.TokenType.INSTRUCTION, value);
		}
	}
}