package com.shiftingdawn.feylon.syntax;

import com.shiftingdawn.feylon.OrderedList;

import java.util.Collection;
import java.util.SequencedCollection;
import java.util.function.IntPredicate;

public class Parser {

	public static SequencedCollection<Token> parseProgram(final String file, final Collection<String> lines) {
		final OrderedList<Token> result = new OrderedList<>();
		int lineNr = 0;
		for (final String line : lines) {
			result.addAll(Parser.parseLine(file, lineNr++, line));
		}
		return result;
	}

	private static Collection<Token> parseLine(final String file, final int lineNr, final String line) {
		final OrderedList<Token> result = new OrderedList<>();
		int pos = Parser.find(line, 0, x -> !Character.isWhitespace(x));
		while (pos < line.length()) {
			final TokenPos tokenPos = new TokenPos(file, lineNr, pos);
			if (line.charAt(pos) == '"') {
				final int endPos = Parser.find(line, pos + 1, x -> x == '"');
				final String fullToken = line.substring(pos, endPos + 1);
				result.append(new Token(tokenPos, TokenType.STRING, fullToken, fullToken.substring(1, fullToken.length() - 1)));
				pos = Parser.find(line, endPos + 2, x -> !Character.isWhitespace(x));
			} else {
				final int endPos = Parser.find(line, pos, Character::isWhitespace);
				final String fullToken = line.substring(pos, endPos);
				try {
					result.append(new Token(tokenPos, TokenType.INTEGER, fullToken, Integer.parseInt(fullToken)));
				} catch (final NumberFormatException ignored) {
					Intrinsic.getByText(fullToken).ifPresentOrElse(intrinsic -> {
						result.append(new Token(tokenPos, TokenType.INTRINSIC, fullToken, intrinsic));
					}, () -> Keyword.getByText(fullToken).ifPresentOrElse(keyword -> {
						result.append(new Token(tokenPos, TokenType.KEYWORD, fullToken, keyword));
					}, () -> {
						result.append(new Token(tokenPos, TokenType.INSTRUCTION, fullToken, fullToken));
					}));
				}
				pos = Parser.find(line, endPos, x -> !Character.isWhitespace(x));
			}
		}
		return result;
	}

	private static int find(final String line, int start, final IntPredicate predicate) {
		while (start < line.length() && !predicate.test(line.charAt(start))) {
			++start;
		}
		return start;
	}
}