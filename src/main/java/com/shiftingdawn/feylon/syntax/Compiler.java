package com.shiftingdawn.feylon.syntax;

import com.shiftingdawn.feylon.OrderedList;
import com.shiftingdawn.feylon.Stack;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Compiler {

	public static Program compile(final String file, final Collection<String> lines) {
		final SequencedCollection<Token> parsedProgram = Parser.parseProgram(file, lines);
		final SourceStack sourceStack = Compiler.makeSourceStack(parsedProgram);
		return new Program(Assembler.assemble(sourceStack));
	}

	public record SourceStack(InstructionSource[] sources, Map<String, Integer> functions) {
	}

	private static SourceStack makeSourceStack(final SequencedCollection<Token> parsedProgram) {
		final OrderedList<Token> tokenList = new OrderedList<>(parsedProgram).reverse();
		OrderedList<InstructionSource> result = new OrderedList<>();

		final Stack instructionStack = new Stack();
		final Map<String, Integer> functions = new HashMap<>();
		final Map<String, InstructionSource[]> constants = new HashMap<>();

		String currentConstant = null;
		OrderedList<InstructionSource> constantResultListCache = new OrderedList<>();

		while (!tokenList.isEmpty()) {
			final Token token = tokenList.pop();
			switch (token.type()) {
				case INTEGER -> result.append(new InstructionSource(token, InstructionType.PUSH_INT, token.operand()));
				case STRING -> result.append(new InstructionSource(token, InstructionType.PUSH_STRING, token.operand()));
				case INTRINSIC -> result.append(new InstructionSource(token, InstructionType.INTRINSIC, token.operand()));
				case KEYWORD -> {
					switch ((Keyword) token.operand()) {
						case END -> {
							if (currentConstant != null) {
								constants.put(currentConstant, result.toArray(InstructionSource[]::new));
								result = constantResultListCache;
								currentConstant = null;
								constantResultListCache = new OrderedList<>();
							} else {
								final int pointer = instructionStack.pop();
								final InstructionSource instructionSource = result.get(pointer);
								switch (instructionSource.type) {
									case IF, ELSE -> {
										result.append(new InstructionSource(token, InstructionType.JUMP, result.size() + 1));
										instructionSource.data = result.size();
									}
									case DO -> {
										result.append(new InstructionSource(token, InstructionType.JUMP, (int) instructionSource.data + 1));
										instructionSource.data = result.size();
									}
									case FUNCTION -> {
										result.append(new InstructionSource(token, InstructionType.RETURN, result.size() + 1));
										instructionSource.data = result.size();
									}
									default -> throw new AssertionError(OpType.END + " operation refers to illegal operation " + instructionSource.type);
								}
							}
						}
						case IF -> {
							instructionStack.push(result.size());
							result.append(new InstructionSource(token, InstructionType.IF, null));
						}
						case ELSE -> {
							final int pointer = instructionStack.pop();
							instructionStack.push(result.size());
							final InstructionSource instructionSource = result.get(pointer);
							if (instructionSource.type != InstructionType.IF) {
								throw new AssertionError(Keyword.ELSE + " keyword refers to illegal operation " + instructionSource.type);
							}
							result.append(new InstructionSource(token, InstructionType.ELSE, result.size()));
							instructionSource.data = result.size();
						}
						case WHILE -> {
							instructionStack.push(result.size());
							result.append(new InstructionSource(token, InstructionType.WHILE, null));
						}
						case DO -> {
							final int pointer = instructionStack.pop();
							instructionStack.push(result.size());
							result.append(new InstructionSource(token, InstructionType.DO, pointer));
						}
						case FUNCTION -> {
							final int selfPointer = result.size();
							final Token nextToken = tokenList.pop();
							if (nextToken.type() != TokenType.INSTRUCTION) {
								throw new AssertionError("Expected function name, got: " + nextToken.type());
							}
							instructionStack.push(selfPointer);
							result.append(new InstructionSource(token, InstructionType.FUNCTION, null));
							functions.put(nextToken.txt(), selfPointer + 1);
						}
						case IMPORT -> {
							final Token nextToken = tokenList.pop();
							if (nextToken.type() != TokenType.STRING) {
								throw new AssertionError("Expected import path, got: " + nextToken.type());
							}
							final File selfDir = new File(token.pos().file()).getParentFile();
							final Path importPath = selfDir.toPath().resolve((String) nextToken.operand());
							if (!Files.exists(importPath)) {
								throw new AssertionError("Import does not exist: " + importPath);
							}
							try {
								final Collection<String> importLines = Files.readAllLines(importPath);
								final SequencedCollection<Token> importStack = Parser.parseProgram(importPath.toString(), importLines);
								final OrderedList<Token> importTokenList = new OrderedList<>(importStack).reverse();
								tokenList.addAll(importTokenList);
							} catch (final IOException e) {
								throw new AssertionError("Could not load import: " + importPath, e);
							}
						}
						case CONST -> {
							final Token nextToken = tokenList.pop();
							if (nextToken.type() != TokenType.INSTRUCTION) {
								throw new AssertionError("Expected function name, got: " + nextToken.type());
							}
							currentConstant = nextToken.txt();
							constantResultListCache = result;
							result = new OrderedList<>();
						}
						default -> throw new AssertionError("Found unimplemented keyword " + token.type());
					}
				}
				case INSTRUCTION -> {
					if (constants.containsKey(token.txt())) {
						result.addAll(Arrays.asList(constants.get(token.txt())));
					} else {
						result.append(new InstructionSource(token, InstructionType.INSTRUCTION, token.operand()));
					}
				}
				default -> throw new AssertionError("Encountered unhandled AbstractToken: " + token.getClass().getName());
			}
		}
		return new SourceStack(result.toArray(InstructionSource[]::new), functions);
	}
}
