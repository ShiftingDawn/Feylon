package com.shiftingdawn.feylon;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

class Lexer {

	public enum LexedTokenType {
		INT, INSTRUCTION, STRING;
	}

	private record LexedLineToken(int pos, LexedTokenType type, Object value) {
	}

	public record LexedLinePosition(String file, int lineNr, int charPos) {
	}

	public record LexedLine(LexedTokenType type, LexedLinePosition pos, Object value) {
	}

	private static int findPosition(final String line, int start, final Predicate<Character> predicate) {
		while (start < line.length() && !predicate.test(line.charAt(start))) {
			++start;
		}
		return start;
	}

	public static LexedLine[] lexLines(final String fileName, final String[] lines) {
		final ArrayList<LexedLine> result = new ArrayList<>();
		for (int row = 0; row < lines.length; ++row) {
			final LexedLineToken[] tokens = Lexer.lexLine(lines[row].split("//", 2)[0]);
			for (final LexedLineToken token : tokens) {
				result.add(new LexedLine(token.type, new LexedLinePosition(fileName, row + 1, token.pos + 1), token.value));
			}
		}
		return result.toArray(new LexedLine[0]);
	}

	private static LexedLineToken[] lexLine(final String line) {
		final List<LexedLineToken> result = new ArrayList<>();
		int pos = Lexer.findPosition(line, 0, x -> x != ' ');
		while (pos < line.length()) {
			if (line.charAt(pos) == '"') {
				final int endPos = Lexer.findPosition(line, pos + 1, x -> x == '"');
				assert line.charAt(endPos) == '"';
				final String tokenText = line.substring(pos + 1, endPos);
				result.add(new LexedLineToken(pos, LexedTokenType.STRING, tokenText));
				pos = Lexer.findPosition(line, endPos + 2, x -> x != ' ');
			} else {
				final int endPos = Lexer.findPosition(line, pos, x -> x == ' ');
				final String tokenText = line.substring(pos, endPos);
				try {
					result.add(new LexedLineToken(pos, LexedTokenType.INT, Integer.parseInt(tokenText)));
				} catch (final NumberFormatException ignored) {
					result.add(new LexedLineToken(pos, LexedTokenType.INSTRUCTION, tokenText));
				}
				pos = Lexer.findPosition(line, endPos, x -> x != ' ');
			}
		}
		return result.toArray(LexedLineToken[]::new);
	}
}