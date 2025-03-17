package com.shiftingdawn.feylon.syntax;

import com.shiftingdawn.feylon.Stack;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Compiler {

	public static Program compile(final String fileName, final Collection<String> lines) {
		final Tokenizer.TokenStack tokenStack = Tokenizer.tokenize(fileName, lines.toArray(String[]::new));
		final SourceStack sourceStack = Compiler.makeSourceStack(tokenStack);
		return new Program(Assembler.assemble(sourceStack));
	}

	public record SourceStack(InstructionSource[] sources, Map<String, Integer> functions) {
	}

	public static class InstructionSource {

		public final OpType type;
		public Object data;

		public InstructionSource(final OpType type, final Object data) {
			this.type = type;
			this.data = data;
		}
	}

	private static SourceStack makeSourceStack(final Tokenizer.TokenStack tokenStack) {
		final InstructionSource[] result = new InstructionSource[tokenStack.tokenized().length];
		final Stack instructionStack = new Stack();
		final Map<String, Integer> functions = new HashMap<>();
		for (int i = 0; i < tokenStack.tokenized().length; ++i) {
			final Tokenizer.Token token = tokenStack.tokenized()[i];
			switch (token.type()) {
				case INT -> result[i] = new InstructionSource(OpType.PUSH_INT, token.value());
				case STRING -> result[i] = new InstructionSource(OpType.PUSH_STRING, token.value());
				case KEYWORD -> {
					switch ((Keyword) token.value()) {
						case END -> {
							final int pointer = instructionStack.pop();
							final InstructionSource instructionSource = result[pointer];
							if (instructionSource.type == OpType.IF || instructionSource.type == OpType.ELSE) {
								instructionSource.data = i;
								result[i] = new InstructionSource(OpType.END, i + 1);
							} else if (instructionSource.type == OpType.DO) {
								result[i] = new InstructionSource(OpType.END, (int) instructionSource.data + 1);
								instructionSource.data = i + 1;
							} else if (instructionSource.type == OpType.FUNCTION) {
								result[i] = new InstructionSource(OpType.RETURN, i + 1);
								instructionSource.data = i + 1;
							} else {
								throw new AssertionError(OpType.END + " operation refers to illegal operation " + instructionSource.type);
							}
						}
						case IF -> {
							instructionStack.push(i);
							result[i] = new InstructionSource(OpType.IF, null);
						}
						case ELSE -> {
							final int pointer = instructionStack.pop();
							instructionStack.push(i);
							final InstructionSource instructionSource = result[pointer];
							if (instructionSource.type != OpType.IF) {
								throw new AssertionError(OpType.ELSE + " operation refers to illegal operation " + instructionSource.type);
							}
							instructionSource.data = i + 1;
							result[i] = new InstructionSource(OpType.ELSE, i);
						}
						case WHILE -> {
							instructionStack.push(i);
							result[i] = new InstructionSource(OpType.WHILE, null);
						}
						case DO -> {
							final int pointer = instructionStack.pop();
							instructionStack.push(i);
							result[i] = new InstructionSource(OpType.DO, pointer);
						}
						case FUNCTION -> {
							final Tokenizer.Token nextToken = tokenStack.tokenized()[i + 1];
							if (nextToken.type() != Tokenizer.TokenType.INSTRUCTION) {
								throw new AssertionError("Expected function name, got: " + nextToken.type());
							}
							result[i] = new InstructionSource(OpType.FUNCTION, null);
							instructionStack.push(i);
							functions.put((String) nextToken.value(), i + 2);
							i += 1;
						}
						default -> throw new AssertionError("Found unimplemented keyword " + token.value());
					}
				}
				case INTRINSIC -> result[i] = new InstructionSource(OpType.INTRINSIC, token.value());
				case INSTRUCTION -> result[i] = new InstructionSource(OpType.OPERATION, token.value());
			}
		}
		return new SourceStack(result, functions);
	}
}
