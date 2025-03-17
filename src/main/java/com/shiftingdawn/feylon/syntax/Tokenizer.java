package com.shiftingdawn.feylon.syntax;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

class Tokenizer {

	public enum TokenType {
		INT, STRING, KEYWORD, INTRINSIC, INSTRUCTION;
	}

	public record TokenStack(Token[] tokenized) {
	}

	public record Token(Location location, TokenType type, Object value) {
	}

	public static TokenStack tokenize(final String filePath, final Collection<String> lines) {
		final ArrayList<Token> result = new ArrayList<>();
		int row = 0;
		for (final String line : lines) {
			final ParsedLineToken[] tokens = Tokenizer.parseLine(line.split("//", 2)[0]);
			for (final ParsedLineToken token : tokens) {
				result.add(new Token(new Location(filePath, ++row, token.pos + 1), token.type, token.value));
			}
		}
		return new TokenStack(result.toArray(Token[]::new));
	}

	private record ParsedLineToken(int pos, TokenType type, Object value) {
	}

	private static ParsedLineToken[] parseLine(final String line) {
		final List<ParsedLineToken> result = new ArrayList<>();
		int pos = Tokenizer.findPosition(line, 0, x -> x != ' ');
		while (pos < line.length()) {
			if (line.charAt(pos) == '"') {
				final int endPos = Tokenizer.findPosition(line, pos + 1, x -> x == '"');
				assert line.charAt(endPos) == '"';
				final String tokenText = line.substring(pos + 1, endPos);
				result.add(new ParsedLineToken(pos, TokenType.STRING, tokenText));
				pos = Tokenizer.findPosition(line, endPos + 2, x -> x != ' ');
			} else {
				final int endPos = Tokenizer.findPosition(line, pos, x -> x == ' ');
				final String tokenText = line.substring(pos, endPos);
				try {
					result.add(new ParsedLineToken(pos, TokenType.INT, Integer.parseInt(tokenText)));
				} catch (final NumberFormatException ignored) {
					final Optional<Keyword> keyword = Keyword.getByText(tokenText);
					if (keyword.isPresent()) {
						result.add(new ParsedLineToken(pos, TokenType.KEYWORD, keyword.get()));
					} else {
						final Optional<Intrinsic> intrinsic = Intrinsic.getByText(tokenText);
						if (intrinsic.isPresent()) {
							result.add(new ParsedLineToken(pos, TokenType.INTRINSIC, intrinsic.get()));
						} else {
							result.add(new ParsedLineToken(pos, TokenType.INSTRUCTION, tokenText));
						}
					}
				}
				pos = Tokenizer.findPosition(line, endPos, x -> x != ' ');
			}
		}
		return result.toArray(ParsedLineToken[]::new);
	}

	private static int findPosition(final String line, int start, final Predicate<Character> predicate) {
		while (start < line.length() && !predicate.test(line.charAt(start))) {
			++start;
		}
		return start;
	}
}