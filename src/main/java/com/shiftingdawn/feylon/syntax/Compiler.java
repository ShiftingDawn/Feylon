package com.shiftingdawn.feylon.syntax;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.shiftingdawn.feylon.OrderedList;
import com.shiftingdawn.feylon.Stack;
import com.shiftingdawn.feylon.lang.Intrinsics;
import com.shiftingdawn.feylon.lang.Keywords;
import com.shiftingdawn.feylon.lang.LexedProgramSource;
import com.shiftingdawn.feylon.lang.Lexer;
import com.shiftingdawn.feylon.lang.RawToken;
import com.shiftingdawn.feylon.lang.RawTokenType;
import com.shiftingdawn.feylon.lang.TokenPos;

public class Compiler {

	private static final List<String> TOKENS_TO_REMOVE = Arrays.asList(
			Intrinsics.CAST_INTEGER.textValue, Intrinsics.CAST_BOOLEAN.textValue, Intrinsics.CAST_POINTER.textValue
	);

	public static Program compile(final String file, final Collection<String> lines, final int shutdownStackSize) {
		final LexedProgramSource programSource = Lexer.lex(file, lines);

		final CompilerContext ctx = new CompilerContext();
		Compiler.makeSourceStack(ctx, programSource);

		TypeChecker.check(ctx, shutdownStackSize);
		for (int i = 0; i < ctx.instructions.size(); ++i) {
			if (Compiler.TOKENS_TO_REMOVE.contains(ctx.instructions.get(i).txt)) {
				ctx.instructions.set(i, null);
			}
		}
		return new Program(Assembler.assemble(ctx));
	}

	public static Program compile(final String file, final Collection<String> lines) {
		return Compiler.compile(file, lines, 0);
	}

	private static void makeSourceStack(final CompilerContext ctx, final LexedProgramSource programSource) {
		final OrderedList<RawToken> tokenList = new OrderedList<>(programSource.tokens()).reverse();
		final Stack instructionStack = new Stack();

		while (!tokenList.isEmpty()) {
			RawToken token = tokenList.pop();
			switch (token.type()) {
				case INT -> ctx.instructions.append(new InstructionSource(token, InstructionType.PUSH_INT, token.operand()));
				case STRING -> ctx.instructions.append(new InstructionSource(token, InstructionType.PUSH_STRING, token.operand()));
				case INTRINSIC -> ctx.instructions.append(new InstructionSource(token, InstructionType.INTRINSIC, token.operand()));
				case KEYWORD -> {
					switch ((Keywords) token.operand()) {
						case END -> {
							final int pointer = instructionStack.pop();
							final InstructionSource instructionSource = ctx.instructions.get(pointer);
							switch (instructionSource.type) {
								case IF, ELSE -> {
									ctx.instructions.append(new InstructionSource(token, InstructionType.JUMP, ctx.instructions.size() + 1));
									instructionSource.data = ctx.instructions.size();
								}
								case DO -> {
									ctx.instructions.append(new InstructionSource(token, InstructionType.JUMP, (int) instructionSource.data + 1));
									instructionSource.data = ctx.instructions.size();
								}
								case FUNCTION -> {
									ctx.instructions.append(new InstructionSource(token, InstructionType.RETURN, ctx.instructions.size() + 1));
									instructionSource.data = ctx.instructions.size();
								}
								default -> throw new AssertionError(Keywords.END + " operation refers to illegal operation " + instructionSource.type);
							}
						}
						case IF -> {
							instructionStack.push(ctx.instructions.size());
							ctx.instructions.append(new InstructionSource(token, InstructionType.IF, null));
						}
						case ELSE -> {
							final int pointer = instructionStack.pop();
							instructionStack.push(ctx.instructions.size());
							final InstructionSource instructionSource = ctx.instructions.get(pointer);
							if (instructionSource.type != InstructionType.IF) {
								throw new AssertionError(Keywords.ELSE + " keyword refers to illegal operation " + instructionSource.type);
							}
							ctx.instructions.append(new InstructionSource(token, InstructionType.ELSE, ctx.instructions.size()));
							instructionSource.data = ctx.instructions.size();
						}
						case WHILE -> {
							instructionStack.push(ctx.instructions.size());
							ctx.instructions.append(new InstructionSource(token, InstructionType.WHILE, null));
						}
						case DO -> {
							final int pointer = instructionStack.pop();
							instructionStack.push(ctx.instructions.size());
							ctx.instructions.append(new InstructionSource(token, InstructionType.DO, pointer));
						}
						case FUNCTION -> {
							final int selfPointer = ctx.instructions.size();
							final RawToken nextToken = tokenList.pop();
							if (nextToken.type()!=RawTokenType.FUNCTION_NAME) {
								throw new AssertionError("Expected function name, got: " + nextToken.type());
							}
							instructionStack.push(selfPointer);
							ctx.instructions.append(new InstructionSource(token, InstructionType.FUNCTION, null));
							final String funcName = nextToken.txt();
							final PositionedType[] inputs = Arrays.stream(programSource.funcs().get(funcName).inputs())
									.map(type -> new PositionedType(type, nextToken.pos()))
									.toArray(PositionedType[]::new);
							final PositionedType[] outputs = Arrays.stream(programSource.funcs().get(funcName).outputs())
									.map(type -> new PositionedType(type, nextToken.pos()))
									.toArray(PositionedType[]::new);
							ctx.functions.put(funcName, new FunctionDef(token.pos(), selfPointer + 1, inputs, outputs));
						}
						case IMPORT -> {
							final RawToken nextToken = tokenList.pop();
							if (nextToken.type()!=RawTokenType.STRING) {
								throw new AssertionError("Expected import path, got: " + nextToken.type());
							}
							final File selfDir = new File(token.pos().file()).getParentFile();
							final Path importPath = selfDir.toPath().resolve((String) nextToken.operand());
							if (!Files.exists(importPath)) {
								throw new AssertionError("Import does not exist: " + importPath);
							}
							try {
								final Collection<String> importLines = Files.readAllLines(importPath);
								final LexedProgramSource importedProgramSource = Lexer.lex(importPath.toString(), importLines);
								final OrderedList<RawToken> importTokenList = new OrderedList<>(importedProgramSource.tokens()).reverse();
								tokenList.addAll(importTokenList);
								programSource.funcs().putAll(importedProgramSource.funcs());
							} catch (final IOException e) {
								throw new AssertionError("Could not load import: " + importPath, e);
							}
						}
						case CONST -> {
							token = tokenList.pop();
							if (token.type()!=RawTokenType.CONST_NAME) {
								throw new AssertionError("Expected function name, got: " + token.type());
							}
							final String constantName = token.txt();
							final TokenPos constLocation = token.pos();
							final Map.Entry<Integer, DataType> constData = Prevaluator.evaluateConstant(constLocation, ctx, tokenList);
							ctx.constants.put(constantName, new ConstantDef(constLocation, constData.getValue(), constData.getKey()));
						}
						default -> throw new AssertionError("Found unimplemented keyword " + token.type());
					}
				}
				case OTHER -> {
					if (ctx.functions.containsKey(token.txt())) {
						ctx.instructions.append(new InstructionSource(token, InstructionType.CALL, token.txt()));
					} else if (ctx.constants.containsKey(token.txt())) {
						final ConstantDef constDef = ctx.constants.get(token.txt());
						ctx.instructions.append(switch (constDef.dataType()) {
							case INTEGER -> new InstructionSource(token, InstructionType.PUSH_INT, constDef.value());
							case BOOLEAN -> throw new AssertionError("Not implemented yet");
							case POINTER -> throw new AssertionError("Not implemented yet");
						});
					} else {
						ctx.instructions.append(new InstructionSource(token, InstructionType.INSTRUCTION, token.txt()));
					}
				}
				default -> throw new AssertionError("Encountered unhandled AbstractToken: " + token.getClass().getName());
			}
		}
	}
}
