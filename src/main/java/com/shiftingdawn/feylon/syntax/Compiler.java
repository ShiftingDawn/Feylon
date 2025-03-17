package com.shiftingdawn.feylon.syntax;

import com.shiftingdawn.feylon.OrderedList;
import com.shiftingdawn.feylon.Stack;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Compiler {

	public static Program compile(final String filePath, final Collection<String> lines) {
		final Tokenizer.TokenStack tokenStack = Tokenizer.tokenize(filePath, lines);
		final SourceStack sourceStack = Compiler.makeSourceStack(tokenStack);
		return new Program(Assembler.assemble(sourceStack));
	}

	public record SourceStack(InstructionSource[] sources, Map<String, Integer> functions) {
	}

	public static class InstructionSource {

		public final OpType type;
		public final Location location;
		public Object data;

		public InstructionSource(final OpType type, final Location location, final Object data) {
			this.type = type;
			this.location = location;
			this.data = data;
		}
	}

	private static SourceStack makeSourceStack(final Tokenizer.TokenStack tokenStack) {
		final OrderedList<AbstractToken<?>> tokenList = new OrderedList<>(tokenStack.tokenized());
		Collections.reverse(tokenList);
		List<InstructionSource> result = new ArrayList<>();

		final Stack instructionStack = new Stack();
		final Map<String, Integer> functions = new HashMap<>();
		final Map<String, InstructionSource[]> constants = new HashMap<>();

		String currentConstant = null;
		List<InstructionSource> constantResultListCache = new ArrayList<>();

		while (!tokenList.isEmpty()) {
			final AbstractToken<?> token = tokenList.pop();
			switch (token) {
				case final AbstractToken.Int intToken -> result.addLast(new InstructionSource(OpType.PUSH_INT, token.location, intToken.value));
				case final AbstractToken.String stringToken -> result.addLast(new InstructionSource(OpType.PUSH_STRING, token.location, stringToken.value));
				case final AbstractToken.Keyword keywordToken -> {
					switch (keywordToken.value) {
						case END -> {
							if (currentConstant != null) {
								constants.put(currentConstant, result.toArray(InstructionSource[]::new));
								result = constantResultListCache;
								currentConstant = null;
								constantResultListCache = new ArrayList<>();
							} else {
								final int pointer = instructionStack.pop();
								final InstructionSource instructionSource = result.get(pointer);
								switch (instructionSource.type) {
									case IF, ELSE -> {
										result.addLast(new InstructionSource(OpType.END, token.location, result.size() + 1));
										instructionSource.data = result.size();
									}
									case DO -> {
										result.addLast(new InstructionSource(OpType.END, token.location, (int) instructionSource.data + 1));
										instructionSource.data = result.size();
									}
									case FUNCTION -> {
										result.addLast(new InstructionSource(OpType.RETURN, token.location, result.size() + 1));
										instructionSource.data = result.size();
									}
									default -> throw new AssertionError(OpType.END + " operation refers to illegal operation " + instructionSource.type);
								}
							}
						}
						case IF -> {
							instructionStack.push(result.size());
							result.addLast(new InstructionSource(OpType.IF, token.location, null));
						}
						case ELSE -> {
							final int pointer = instructionStack.pop();
							instructionStack.push(result.size());
							final InstructionSource instructionSource = result.get(pointer);
							if (instructionSource.type != OpType.IF) {
								throw new AssertionError(OpType.ELSE + " operation refers to illegal operation " + instructionSource.type);
							}
							result.addLast(new InstructionSource(OpType.ELSE, token.location, result.size()));
							instructionSource.data = result.size();
						}
						case WHILE -> {
							instructionStack.push(result.size());
							result.addLast(new InstructionSource(OpType.WHILE, token.location, null));
						}
						case DO -> {
							final int pointer = instructionStack.pop();
							instructionStack.push(result.size());
							result.addLast(new InstructionSource(OpType.DO, token.location, pointer));
						}
						case FUNCTION -> {
							final int selfPointer = result.size();
							final AbstractToken<?> nextToken = tokenList.pop();
							if (!(nextToken instanceof final AbstractToken.Instruction instructionToken)) {
								throw new AssertionError("Expected function name, got: " + nextToken.type);
							}
							instructionStack.push(selfPointer);
							result.addLast(new InstructionSource(OpType.FUNCTION, token.location, null));
							functions.put(instructionToken.value, selfPointer + 1);
						}
						case IMPORT -> {
							final AbstractToken<?> nextToken = tokenList.pop();
							if (!(nextToken instanceof final AbstractToken.String stringToken)) {
								throw new AssertionError("Expected import path, got: " + nextToken.type);
							}
							final File selfDir = new File(token.location.filePath()).getParentFile();
							final Path importPath = selfDir.toPath().resolve(stringToken.value);
							if (!Files.exists(importPath)) {
								throw new AssertionError("Import does not exist: " + importPath);
							}
							try {
								final Collection<String> importLines = Files.readAllLines(importPath);
								final Tokenizer.TokenStack importStack = Tokenizer.tokenize(importPath.toString(), importLines);
								final OrderedList<AbstractToken<?>> importTokenList = new OrderedList<>(importStack.tokenized());
								Collections.reverse(importTokenList);
								tokenList.addAll(importTokenList);
							} catch (final IOException e) {
								throw new AssertionError("Could not load import: " + importPath, e);
							}
						}
						case CONST -> {
							final AbstractToken<?> nextToken = tokenList.pop();
							if (!(nextToken instanceof final AbstractToken.Instruction instructionToken)) {
								throw new AssertionError("Expected function name, got: " + nextToken.type);
							}
							currentConstant = instructionToken.value;
							constantResultListCache = result;
							result = new ArrayList<>();
						}
						default -> throw new AssertionError("Found unimplemented keyword " + token.value);
					}
				}
				case final AbstractToken.Intrinsic intrinsicToken -> result.addLast(new InstructionSource(OpType.INTRINSIC, token.location, intrinsicToken.value));
				case final AbstractToken.Instruction instructionToken -> {
					if (constants.containsKey(instructionToken.value)) {
						result.addAll(Arrays.asList(constants.get(instructionToken.value)));
					} else {
						result.addLast(new InstructionSource(OpType.OPERATION, token.location, instructionToken.value));
					}
				}
				default -> throw new AssertionError("Encountered unhandled AbstractToken: " + token.getClass().getName());
			}
		}
		return new SourceStack(result.toArray(InstructionSource[]::new), functions);
	}
}
