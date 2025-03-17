package com.shiftingdawn.feylon.syntax;

public abstract class AbstractParserElement<T> {

	public final int pos;
	public final Tokenizer.TokenType type;
	public final T value;

	public AbstractParserElement(final int pos, final Tokenizer.TokenType type, final T value) {
		this.pos = pos;
		this.type = type;
		this.value = value;
	}

	public static class Int extends AbstractParserElement<Integer> {

		public Int(final int pos, final int value) {
			super(pos, Tokenizer.TokenType.INT, value);
		}
	}

	public static class String extends AbstractParserElement<java.lang.String> {

		public String(final int pos, final java.lang.String value) {
			super(pos, Tokenizer.TokenType.STRING, value);
		}
	}

	public static class Keyword extends AbstractParserElement<com.shiftingdawn.feylon.syntax.Keyword> {

		public Keyword(final int pos, final com.shiftingdawn.feylon.syntax.Keyword value) {
			super(pos, Tokenizer.TokenType.KEYWORD, value);
		}
	}

	public static class Intrinsic extends AbstractParserElement<com.shiftingdawn.feylon.syntax.Intrinsic> {

		public Intrinsic(final int pos, final com.shiftingdawn.feylon.syntax.Intrinsic value) {
			super(pos, Tokenizer.TokenType.INTRINSIC, value);
		}
	}

	public static class Instruction extends AbstractParserElement<java.lang.String> {

		public Instruction(final int pos, final java.lang.String value) {
			super(pos, Tokenizer.TokenType.INSTRUCTION, value);
		}
	}
}