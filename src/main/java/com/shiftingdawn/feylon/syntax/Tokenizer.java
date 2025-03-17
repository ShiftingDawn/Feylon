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

	public record TokenStack(List<AbstractToken<?>> tokenized) {
	}

	public static TokenStack tokenize(final String filePath, final Collection<String> lines) {
		final ArrayList<AbstractToken<?>> result = new ArrayList<>();
		int row = 0;
		for (final String line : lines) {
			final Collection<AbstractParserElement<?>> tokens = Tokenizer.parseLine(line.split("//", 2)[0]);
			for (final AbstractParserElement<?> token : tokens) {
				final Location loc = new Location(filePath, ++row, token.pos + 1);
				result.add(switch (token) {
					case final AbstractParserElement.Int intToken -> new AbstractToken.Int(loc, intToken.value);
					case final AbstractParserElement.String stringToken -> new AbstractToken.String(loc, stringToken.value);
					case final AbstractParserElement.Keyword keywordToken -> new AbstractToken.Keyword(loc, keywordToken.value);
					case final AbstractParserElement.Intrinsic stringToken -> new AbstractToken.Intrinsic(loc, stringToken.value);
					case final AbstractParserElement.Instruction instructionToken -> new AbstractToken.Instruction(loc, instructionToken.value);
					default -> throw new AssertionError("Found unhandled token type: " + token.getClass().getName());
				});
			}
		}
		return new TokenStack(result);
	}

	private static Collection<AbstractParserElement<?>> parseLine(final String line) {
		final List<AbstractParserElement<?>> result = new ArrayList<>();
		int pos = Tokenizer.findPosition(line, 0, x -> x != ' ');
		while (pos < line.length()) {
			if (line.charAt(pos) == '"') {
				final int endPos = Tokenizer.findPosition(line, pos + 1, x -> x == '"');
				assert line.charAt(endPos) == '"';
				final String tokenText = line.substring(pos + 1, endPos);
				result.add(new AbstractParserElement.String(pos, tokenText));
				pos = Tokenizer.findPosition(line, endPos + 2, x -> x != ' ');
			} else {
				final int endPos = Tokenizer.findPosition(line, pos, x -> x == ' ');
				final String tokenText = line.substring(pos, endPos);
				try {
					result.add(new AbstractParserElement.Int(pos, Integer.parseInt(tokenText)));
				} catch (final NumberFormatException ignored) {
					final Optional<Keyword> keyword = Keyword.getByText(tokenText);
					if (keyword.isPresent()) {
						result.add(new AbstractParserElement.Keyword(pos, keyword.get()));
					} else {
						final Optional<Intrinsic> intrinsic = Intrinsic.getByText(tokenText);
						if (intrinsic.isPresent()) {
							result.add(new AbstractParserElement.Intrinsic(pos, intrinsic.get()));
						} else {
							result.add(new AbstractParserElement.Instruction(pos, tokenText));
						}
					}
				}
				pos = Tokenizer.findPosition(line, endPos, x -> x != ' ');
			}
		}
		return result;
	}

	private static int findPosition(final String line, int start, final Predicate<Character> predicate) {
		while (start < line.length() && !predicate.test(line.charAt(start))) {
			++start;
		}
		return start;
	}
}