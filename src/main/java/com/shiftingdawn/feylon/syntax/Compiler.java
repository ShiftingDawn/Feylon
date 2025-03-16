package com.shiftingdawn.feylon.syntax;

import com.shiftingdawn.feylon.Stack;

import java.util.Collection;

public class Compiler {

	public static Program compile(final String fileName, final Collection<String> lines) {
		final Tokenizer.TokenStack tokenStack = Tokenizer.tokenize(fileName, lines.toArray(String[]::new));
		final InstructionSource[] sourceStack = Compiler.makeSourceStack(tokenStack);
		return new Program(Assembler.assemble(sourceStack));
	}

	public static class InstructionSource {

		public final OpType type;
		public Object data;

		public InstructionSource(final OpType type, final Object data) {
			this.type = type;
			this.data = data;
		}
	}

	private static InstructionSource[] makeSourceStack(final Tokenizer.TokenStack tokenStack) {
		final Stack instructionStack = new Stack();
		final InstructionSource[] result = new InstructionSource[tokenStack.tokenized().length];
		for (int i = 0; i < tokenStack.tokenized().length; ++i) {
			final Tokenizer.Token token = tokenStack.tokenized()[i];
			switch (token.type()) {
				case INT -> result[i] = new InstructionSource(OpType.PUSH_INT, token.value());
				case STRING -> result[i] = new InstructionSource(OpType.PUSH_STRING, token.value());
				case KEYWORD -> {
					switch ((Keyword) token.value()) {
						case END -> {
							final int pointer = instructionStack.pop();
							final InstructionSource tuple = result[pointer];
							if (tuple.type == OpType.IF || tuple.type == OpType.ELSE) {
								tuple.data = i;
								result[i] = new InstructionSource(OpType.END, i + 1);
							} else if (tuple.type == OpType.DO) {
								result[i] = new InstructionSource(OpType.END, (int) tuple.data + 1);
								tuple.data = i + 1;
							} else {
								throw new AssertionError(OpType.END + " operation refers to illegal operation " + tuple.type);
							}
						}
						case IF -> {
							instructionStack.push(i);
							result[i] = new InstructionSource(OpType.IF, null);
						}
						case ELSE -> {
							final int pointer = instructionStack.pop();
							instructionStack.push(i);
							final InstructionSource tuple = result[pointer];
							if (tuple.type != OpType.IF) {
								throw new AssertionError(OpType.ELSE + " operation refers to illegal operation " + tuple.type);
							}
							tuple.data = i + 1;
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
					}
				}
				case INTRINSIC -> result[i] = new InstructionSource(OpType.INTRINSIC, token.value());
				case INSTRUCTION -> result[i] = new InstructionSource(OpType.OPERATION, token.value());
			}
		}
		return result;
	}
}
