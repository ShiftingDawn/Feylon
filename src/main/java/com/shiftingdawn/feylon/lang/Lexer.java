package com.shiftingdawn.feylon.lang;

import com.shiftingdawn.feylon.OrderedList;

import java.util.Iterator;
import java.util.function.IntPredicate;

final class Lexer {

	public static OrderedList<LexedToken> lex(final ResolvedSources sources) {
		final OrderedList<LexedToken> tokens = new OrderedList<>();
		int lineNr = -1;
		for (final Iterator<String> lineIterator = sources.lines().iterator(); lineIterator.hasNext(); ) {
			String line = lineIterator.next();
			++lineNr;
			int pos = Lexer.find(line, 0, x -> !Character.isWhitespace(x));
			while (pos < line.length()) {
				if (line.charAt(pos) == '"') {
					int endPos = Lexer.find(line, pos + 1, x -> x == '"');
					if (endPos >= line.length()) {
						final int startLine = lineNr;
						final int startPos = pos;
						final StringBuilder stringBuilder = new StringBuilder(Lexer.unescape(line.substring(pos))).append('\n');
						pos = 0;
						while (lineIterator.hasNext()) {
							line = lineIterator.next();
							++lineNr;
							endPos = Lexer.find(line, pos + 1, x -> x == '"');
							if (endPos >= line.length()) {
								stringBuilder.append(Lexer.unescape(line)).append('\n');
							} else {
								stringBuilder.append(Lexer.unescape(line), 0, endPos + 1);
								tokens.append(new LexedToken(sources.file(), startLine, startPos, stringBuilder.toString()));
								pos = Lexer.find(line, endPos + 2, x -> !Character.isWhitespace(x));
								break;
							}
						}
					} else {
						tokens.append(new LexedToken(sources.file(), lineNr, pos, Lexer.unescape(line.substring(pos, endPos + 1))));
						pos = Lexer.find(line, endPos + 2, x -> !Character.isWhitespace(x));
						continue;
					}
				}
				final int endPos = Lexer.find(line, pos + 1, Character::isWhitespace);
				final String tokenText = line.substring(pos, endPos);
				if (tokenText.startsWith("//")) {
					break;
				}
				tokens.append(new LexedToken(sources.file(), lineNr, pos, tokenText));
				pos = Lexer.find(line, endPos + 1, x -> !Character.isWhitespace(x));
			}
		}
		return tokens;
	}

	static int find(final String line, int startPos, final IntPredicate predicate) {
		while (startPos < line.length() && !predicate.test(line.charAt(startPos))) {
			++startPos;
		}
		return startPos;
	}

	private static String unescape(final String str) {
		return str.replace("\\t", "\t")
				.replace("\\n", "\n")
				.replace("\\r", "\r");
	}
}