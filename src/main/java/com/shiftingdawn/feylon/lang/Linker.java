package com.shiftingdawn.feylon.lang;

import com.shiftingdawn.feylon.OrderedList;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.AccessDeniedException;

final class Linker {

	public static LinkerContext link(final ParserContext parserContext) {
		final LinkerContext ctx = new LinkerContext(parserContext);
		while (!ctx.tokens.isEmpty()) {
			final Token token = ctx.tokens.pop();
			switch (token.type()) {
				case IMPORT -> Linker.handleImport(ctx, token);
				case INT -> ctx.result.append(new LinkedToken(token.pos(), ctx.pointer++, InstructionType.PUSH_INT, token.txt(), token.data()));
				case BOOL -> ctx.result.append(new LinkedToken(token.pos(), ctx.pointer++, InstructionType.PUSH_BOOL, token.txt(), token.data()));
				case STRING -> ctx.result.append(new LinkedToken(token.pos(), ctx.pointer++, InstructionType.PUSH_STRING, token.txt(), token.data()));
				case CONST_REF -> {
					final ConstDef constant = parserContext.constants.get(token.txt());
					ctx.result.append(switch (constant.dataType()) {
						case INT -> new LinkedToken(token.pos(), ctx.pointer++, InstructionType.PUSH_INT, token.txt(), constant.value());

						default -> throw new AssertionError("Encountered unimplemented datatype '%s' of constant '%s'".formatted(constant.dataType(), token.txt()));
					});
				}
				case INTRINSIC -> ctx.result.append(new LinkedToken(token.pos(), ctx.pointer++, InstructionType.INTRINSIC, token.txt(), token.data()));
				case END -> Linker.processBlock(ctx, token);
				case FUNCTION -> {
					ctx.callStack.push(ctx.pointer);
					ctx.result.append(new LinkedToken(token.pos(), ctx.pointer++, InstructionType.FUNCTION, token.txt(), null));
					ctx.functions.get(token.txt()).pointer = ctx.result.getLast().selfPointer;
				}
				case FUNCTION_CALL -> ctx.result.append(new LinkedToken(token.pos(), ctx.pointer++, InstructionType.CALL, token.txt(), null));
				case IF -> {
					ctx.callStack.push(ctx.pointer);
					ctx.result.append(new LinkedToken(token.pos(), ctx.pointer++, InstructionType.JUMP_NEQ, token.txt(), null));
				}
				case ELSE -> {
					if (ctx.callStack.isEmpty()) {
						throw new FeylonException(token.pos(), "Encountered dangling '%s' statement".formatted(token.type()));
					}
					final int referencePointer = ctx.callStack.pop();
					if (referencePointer < 0 || referencePointer >= ctx.result.size()) {
						throw new FeylonException(token.pos(), "Encountered '%s' statement with an invalid reference. This is a linking error.".formatted(token.type()));
					}
					final LinkedToken reference = ctx.result.get(referencePointer);
					if (reference.type != InstructionType.JUMP_NEQ) {
						throw new FeylonException(token.pos(),
								"Encountered '%s' statement that references an invalid instruction '%s'. This is a linking error.".formatted(token.type(), reference.type));
					}
					reference.data = ctx.pointer + 1; //The if-statement jumps to the else block if the value is false.
					ctx.callStack.push(ctx.pointer);
					ctx.result.append(new LinkedToken(token.pos(), ctx.pointer++, InstructionType.JUMP, token.txt(), null));
				}
				case WHILE -> ctx.callStack.push(ctx.pointer); //While statement itself does nothing
				case DO -> {
					if (ctx.callStack.isEmpty()) {
						throw new FeylonException(token.pos(), "Encountered dangling '%s' statement".formatted(token.type()));
					}
					final int referencePointer = ctx.callStack.pop();
					if (referencePointer < 0 || referencePointer >= ctx.result.size()) {
						throw new FeylonException(token.pos(), "Encountered '%s' statement with an invalid reference. This is a linking error.".formatted(token.type()));
					}
					ctx.callStack.push(ctx.pointer);
					ctx.result.append(new LinkedToken(token.pos(), ctx.pointer++, InstructionType.DO, token.txt(), referencePointer));
				}
				default -> throw new AssertionError("Encountered unhandled token: " + token.type());
			}
		}
		return ctx;
	}

	private static void processBlock(final LinkerContext ctx, final Token token) {
		if (ctx.callStack.isEmpty()) {
			throw new FeylonException(token.pos(), "Encountered dangling '%s' statement".formatted(TokenType.END));
		}
		final int referencePointer = ctx.callStack.pop();
		if (referencePointer < 0 || referencePointer >= ctx.result.size()) {
			throw new FeylonException(token.pos(), "Encountered '%s' statement with an invalid reference. This is a linking error.".formatted(TokenType.END));
		}
		final LinkedToken reference = ctx.result.get(referencePointer);
		switch (reference.type) {
			case FUNCTION -> {
				ctx.result.append(new LinkedToken(token.pos(), ctx.pointer++, InstructionType.RETURN, token.txt(), ctx.pointer));
				reference.data = ctx.pointer; //The FUNCTION instruction skips the block.
			}
			case JUMP_NEQ, JUMP -> reference.data = ctx.pointer;
			case DO -> {
				ctx.result.append(new LinkedToken(token.pos(), ctx.pointer++, InstructionType.JUMP, token.txt(), reference.data));
				reference.data = ctx.pointer; //The DO instruction skips the block if on false values.
			}
			default -> throw new FeylonException(token.pos(),
					"Encountered '%s' statement that references an invalid instruction '%s'. This is a linking error.".formatted(TokenType.END, reference.type));
		}
	}

	private static void handleImport(final LinkerContext ctx, final Token token) {
		try {
			final ResolvedSources resolvedSources = Feylon.readSources((String) token.data(), token.pos().file());
			final OrderedList<LexedToken> lexedImport = Lexer.lex(resolvedSources);
			final ParserContext importTokens = Tokenizer.tokenize(lexedImport);
			ctx.add(importTokens);
		} catch (final FileNotFoundException e) {
			throw new FeylonException(token.pos(), "Imported path does not exist: " + e.getMessage());
		} catch (final AccessDeniedException e) {
			throw new FeylonException(token.pos(), "Could not get read access for import: " + e.getMessage());
		} catch (final IOException e) {
			throw new FeylonException(token.pos(), "Could not read import: " + e.getMessage());
		}
	}
}