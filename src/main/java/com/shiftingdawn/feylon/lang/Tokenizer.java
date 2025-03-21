package com.shiftingdawn.feylon.lang;

import com.shiftingdawn.feylon.OrderedList;

final class Tokenizer {

	public static ParserContext tokenize(final OrderedList<LexedToken> lexedTokens) {
		lexedTokens.reverse();
		final ParserContext ctx = new ParserContext();
		while (!lexedTokens.isEmpty()) {
			final LexedToken token = lexedTokens.pop();
			ctx.result.append(Tokenizer.parseToken(ctx, lexedTokens, token));
		}
		return ctx;
	}

	private static Token parseToken(final ParserContext ctx, final OrderedList<LexedToken> lexedTokens, final LexedToken token) {
		try {
			return new Token(token.pos(), TokenType.INT, token.content(), Integer.parseInt(token.content()));
		} catch (final NumberFormatException ignored) {
		}
		if ("true".equals(token.content()) || "false".equals(token.content())) {
			return new Token(token.pos(), TokenType.BOOL, token.content(), "true".equals(token.content()));
		}
		if (token.content().charAt(0) == '"' && token.content().charAt(token.content().length() - 1) == '"') {
			return new Token(token.pos(), TokenType.STRING, token.content(), token.content().substring(1, token.content().length() - 1));
		}
		if ("import".equals(token.content())) {
			if (lexedTokens.isEmpty()) {
				throw new FeylonException(token.pos(), "Missing import path");
			}
			final Token nextToken = Tokenizer.parseToken(ctx, lexedTokens, lexedTokens.pop());
			if (nextToken.type() != TokenType.STRING) {
				throw new FeylonException(token.pos(), "Expected import path as string, got: " + nextToken.type());
			}
			return new Token(token.pos(), TokenType.IMPORT, token.content(), nextToken.data());
		}
		final Intrinsics intrinsic = Intrinsics.getByText(token.content());
		if (intrinsic != null) {
			return new Token(token.pos(), TokenType.INTRINSIC, token.content(), intrinsic);
		}
		final Keywords keyword = Keywords.getByText(token.content());
		if (keyword != null) {
			return Tokenizer.parseKeyword(ctx, lexedTokens, token, keyword);
		}
		if (ctx.functions.containsKey(token.content())) {
			return new Token(token.pos(), TokenType.FUNCTION_CALL, token.content(), null);
		}
		if (ctx.constants.containsKey(token.content())) {
			return new Token(token.pos(), TokenType.CONST_REF, token.content(), null);
		}
		if (ctx.memories.containsKey(token.content())) {
			return new Token(token.pos(), TokenType.MEMORY_REF, token.content(), null);
		}
		throw new FeylonException(token.pos(), "Unknown token: " + token.content());
	}

	private static Token parseKeyword(final ParserContext ctx, final OrderedList<LexedToken> lexedTokens, final LexedToken token, final Keywords keyword) {
		return switch (keyword) {
			case END -> new Token(token.pos(), TokenType.END, token.content(), null);
			case FUNCTION -> Tokenizer.parseFunction(ctx, lexedTokens, token);
			case CONST -> {
				if (lexedTokens.isEmpty()) {
					throw new FeylonException(token.pos(), "Encountered incomplete constant");
				}
				final String constName = lexedTokens.pop().content();
				ctx.constants.put(constName, null);
				yield new Token(token.pos(), TokenType.CONST, constName, null);
			}
			case MEMORY -> {
				if (lexedTokens.isEmpty()) {
					throw new FeylonException(token.pos(), "Encountered incomplete memory definition");
				}
				final String memoryName = lexedTokens.pop().content();
				ctx.memories.put(memoryName, null);
				yield new Token(token.pos(), TokenType.MEMORY, memoryName, null);
			}
			case IF -> {
				if (lexedTokens.isEmpty()) {
					throw new FeylonException(token.pos(), "Encountered incomplete statement");
				}
				yield new Token(token.pos(), TokenType.IF, token.content(), null);
			}
			case ELSE -> {
				if (lexedTokens.isEmpty()) {
					throw new FeylonException(token.pos(), "Encountered incomplete statement");
				}
				yield new Token(token.pos(), TokenType.ELSE, token.content(), null);
			}
			case WHILE -> {
				if (lexedTokens.isEmpty()) {
					throw new FeylonException(token.pos(), "Encountered incomplete statement");
				}
				yield new Token(token.pos(), TokenType.WHILE, token.content(), null);
			}
			case DO -> {
				if (lexedTokens.isEmpty()) {
					throw new FeylonException(token.pos(), "Encountered incomplete statement");
				}
				yield new Token(token.pos(), TokenType.DO, token.content(), null);
			}
			default -> throw new AssertionError("Encountered unhandled keyword " + keyword);
		};
	}

	private static Token parseFunction(final ParserContext ctx, final OrderedList<LexedToken> lexedTokens, final LexedToken token) {
		if (lexedTokens.isEmpty()) {
			throw new FeylonException(token.pos(), "Missing function signature");
		}
		LexedToken nextToken = lexedTokens.pop();
		if (nextToken.content().contains("\"")) {
			throw new FeylonException(nextToken.pos(), "Function name cannot contain any quotes");
		}
		final OrderedList<LexedToken> parts = new OrderedList<>();
		final char[] buffer = new char[512];
		int ptr = 0;
		MainLoop:
		while (!lexedTokens.isEmpty()) {
			final String str = nextToken.content();
			for (int i = 0; i < str.length(); ++i) {
				final char c = str.charAt(i);
				if (c == '(' || c == ' ' || c == ')') {
					if (ptr > 0) {
						parts.append(new LexedToken(nextToken.pos(), String.valueOf(buffer, 0, ptr)));
						ptr = 0;
					}
					if (c == ')') {
						break MainLoop;
					}
				} else if (c == '-' && i + 1 < str.length() && str.charAt(i + 1) == '>') {
					parts.append(new LexedToken(nextToken.pos(), "->"));
					++i;
					ptr = 0;
				} else {
					buffer[ptr++] = c;
				}
			}
			if (ptr > 0) {
				parts.append(new LexedToken(nextToken.pos(), String.valueOf(buffer, 0, ptr)));
				ptr = 0;
			}
			nextToken = lexedTokens.pop();
		}
		final LexedToken funcName = parts.getFirst();
		parts.removeFirst();
		final OrderedList<TypedPos> inputs = new OrderedList<>();
		final OrderedList<TypedPos> outputs = new OrderedList<>();
		boolean input = true;
		for (final LexedToken part : parts) {
			if (part.content().equals("->")) {
				input = false;
			} else {
				final DataType dataType = DataType.getByText(part.content());
				if (dataType == null) {
					throw new FeylonException(part.pos(), "Function signature contains unknown %s type: %s".formatted(input ? "input" : "output", part.content()));
				}
				(input ? inputs : outputs).append(new TypedPos(part.pos(), dataType));
			}
		}
		ctx.functions.put(funcName.content(), new FunctionSignature(inputs, outputs));
		return new Token(token.pos(), TokenType.FUNCTION, funcName.content(), null);
	}
}