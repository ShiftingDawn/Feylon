package com.shiftingdawn.feylon.lang;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.IntPredicate;

import com.shiftingdawn.feylon.OrderedList;
import com.shiftingdawn.feylon.syntax.CompilerException;
import com.shiftingdawn.feylon.syntax.DataType;

public final class Lexer {

	public static LexedProgramSource lex(final String file, final Collection<String> lines) {
		final OrderedList<LexedPositionalToken> tokens = Lexer.parseFileIntoTokens(file, lines);
		final Map<String, FunctionType> functionTypes = Lexer.parseFunctions(tokens);
		return new LexedProgramSource(Lexer.transformTokens(tokens), functionTypes);
	}

	private static OrderedList<LexedPositionalToken> parseFileIntoTokens(final String file, final Collection<String> lines) {
		final OrderedList<LexedPositionalToken> tokens = new OrderedList<>();
		int lineNr = 0;
		for (final String line : lines) {
			Lexer.parseLineIntoTokens(tokens, file, line, lineNr++);
		}
		return tokens;
	}

	private static OrderedList<LexedPositionalToken> parseLineIntoTokens(final OrderedList<LexedPositionalToken> tokens, final String file, final String line, final int lineNr) {
		int pos = Lexer.find(line, 0, x -> !Character.isWhitespace(x));
		while (pos < line.length()) {
			if (line.charAt(pos)=='"') {
				final int endPos = Lexer.find(line, pos + 1, x -> x=='"');
				tokens.append(new LexedPositionalToken(new TokenPos(file, lineNr, pos), LexedTokenType.STRING, line.substring(pos, endPos + 1)));
				pos = Lexer.find(line, endPos + 2, x -> !Character.isWhitespace(x));
			} else {
				int endPos = Lexer.find(line, pos, Character::isWhitespace);
				final String tokenText = line.substring(pos, endPos);
				tokens.append(new LexedPositionalToken(new TokenPos(file, lineNr, pos), LexedTokenType.OTHER, tokenText));
				pos = endPos + 1;

				if (Keywords.FUNCTION.textValue.equals(tokenText)) {
					pos = Lexer.find(line, pos, x -> !Character.isWhitespace(x));
					if (pos + 1 >= line.length()) {
						throw new CompilerException(new TokenPos(file, lineNr, pos - endPos - 1), "Function is missing a name and type definition");
					}
					endPos = Lexer.find(line, pos, x -> x==')');
					if (endPos==line.length()) {
						throw new CompilerException(new TokenPos(file, lineNr, pos), "Function is missing a type definition");
					}
					endPos += 1;
					tokens.append(new LexedPositionalToken(new TokenPos(file, lineNr, pos), LexedTokenType.FUNCTION_DEF, line.substring(pos, endPos)));
					pos = endPos + 1;
				}
			}
		}
		return tokens;
	}

	private static int find(final String line, int startPos, final IntPredicate searchUntil) {
		while (startPos < line.length() && !searchUntil.test(line.charAt(startPos))) {
			++startPos;
		}
		return startPos;
	}

	private static Map<String, FunctionType> parseFunctions(final OrderedList<LexedPositionalToken> tokens) {
		final Map<String, FunctionType> defs = new HashMap<>();
		for (int i = 0; i < tokens.size(); ++i) {
			final LexedPositionalToken token = tokens.get(i);
			if (token.type()==LexedTokenType.FUNCTION_DEF) {
				final String[] nameAndDef = token.txt().substring(0, token.txt().length() - 1).split("\\(");
				if (nameAndDef.length > 2) {
					throw new CompilerException(token.pos(), "Function has invalid signature");
				}
				final OrderedList<DataType> inputs = new OrderedList<>();
				final OrderedList<DataType> outputs = new OrderedList<>();
				if (nameAndDef.length==2) {
					final OrderedList<LexedPositionalToken> list = Lexer.parseLineIntoTokens(new OrderedList<>(), null, nameAndDef[1], 0);
					boolean processingInputs = true;
					for (final LexedPositionalToken typeTokenCandidate : list) {
						if (typeTokenCandidate.txt().equals(Intrinsics.ARROW.textValue)) {
							processingInputs = false;
							continue;
						}
						final OrderedList<DataType> lst = processingInputs ? inputs:outputs;
						if (typeTokenCandidate.txt().equals(Keywords.TYPE_STR.textValue)) {
							lst.append(DataType.INTEGER);
							lst.append(DataType.POINTER);
						} else {
							final DataType type = DataType.getByText(typeTokenCandidate.txt())
									.orElseThrow(() -> new CompilerException(token.pos(), "Function has invalid type in signature: " + typeTokenCandidate.type()));
							lst.append(type);
						}
					}
				}
				defs.put(nameAndDef[0], new FunctionType(inputs.toArray(DataType[]::new), outputs.toArray(DataType[]::new)));
				tokens.set(i, new LexedPositionalToken(token.pos(), LexedTokenType.FUNCTION_DEF, nameAndDef[0]));
			}
		}
		return defs;
	}

	private static OrderedList<RawToken> transformTokens(final OrderedList<LexedPositionalToken> tokens) {
		final OrderedList<RawToken> result = new OrderedList<>();
		for (final LexedPositionalToken token : tokens) {
			switch (token.type()) {
				case STRING -> result.append(new RawToken(token.pos(), RawTokenType.STRING, token.txt(), token.txt().substring(1, token.txt().length() - 1)));
				case FUNCTION_DEF -> result.append(new RawToken(token.pos(), RawTokenType.FUNCTION_NAME, token.txt(), token.txt()));
				case OTHER -> {
					try {
						result.append(new RawToken(token.pos(), RawTokenType.INT, token.txt(), Integer.parseInt(token.txt())));
						continue;
					} catch (final NumberFormatException ignored) {
					}
					Intrinsics.getByText(token.txt()).ifPresentOrElse(intrinsic -> {
						result.append(new RawToken(token.pos(), RawTokenType.INTRINSIC, token.txt(), intrinsic));
					}, () -> Keywords.getByText(token.txt()).ifPresentOrElse(keyword -> {
						result.append(new RawToken(token.pos(), RawTokenType.KEYWORD, token.txt(), keyword));
					}, () -> {
						result.append(new RawToken(token.pos(), RawTokenType.OTHER, token.txt(), null));
					}));
				}
				default -> throw new AssertionError("Encountered an unregistered LexedPositionalToken type: " + token.type());
			}
		}
		return result;
	}
}
