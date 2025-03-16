package com.shiftingdawn.feylon;

import java.util.Collection;
import java.util.Optional;

public class Parser {

	public static Instruction[] parse(final Collection<String> program) {
		final Lexer.LexedLine[] lines = Lexer.lexLines(null, program.toArray(new String[0]));
		final ProgramTuple[] tuples = Parser.makeTuples(lines);
		return Assembler.assemble(tuples);
	}

	private static ProgramTuple[] makeTuples(final Lexer.LexedLine[] tokenData) {
		final Stack instructionStack = new Stack();
		final ProgramTuple[] result = new ProgramTuple[tokenData.length];
		for (int i = 0; i < tokenData.length; ++i) {
			final Lexer.LexedLine token = tokenData[i];
			switch (token.type()) {
				case INT -> result[i] = new ProgramTuple(Ops.OP_PUSH_INT, token.value());
				case STRING -> result[i] = new ProgramTuple(Ops.OP_PUSH_STRING, token.value());
				case INSTRUCTION -> {
					final Optional<Ops> op = Ops.getBySymbol((String) token.value());
					if (op.isPresent()) {
						switch (op.get()) {
							case OP_END -> {
								final int pointer = instructionStack.pop();
								final ProgramTuple tuple = result[pointer];
								if (tuple.op == Ops.OP_IF || tuple.op == Ops.OP_ELSE) {
									tuple.data = i;
									result[i] = new ProgramTuple(Ops.OP_END, i + 1);
								} else if (tuple.op == Ops.OP_DO) {
									result[i] = new ProgramTuple(Ops.OP_END, (int) tuple.data + 1);
									tuple.data = i + 1;
								} else {
									throw new AssertionError(Ops.OP_END + " operation refers to illegal operation " + tuple.op);
								}
								continue;
							}
							case OP_IF -> {
								instructionStack.push(i);
								result[i] = new ProgramTuple(Ops.OP_IF, null);
								continue;
							}
							case OP_ELSE -> {
								final int pointer = instructionStack.pop();
								instructionStack.push(i);
								final ProgramTuple tuple = result[pointer];
								if (tuple.op != Ops.OP_IF) {
									throw new AssertionError(Ops.OP_ELSE + " operation refers to illegal operation " + tuple.op);
								}
								tuple.data = i + 1;
								result[i] = new ProgramTuple(Ops.OP_ELSE, i);
								continue;
							}
							case OP_WHILE -> {
								instructionStack.push(i);
								result[i] = new ProgramTuple(Ops.OP_WHILE, null);
								continue;
							}
							case OP_DO -> {
								final int pointer = instructionStack.pop();
								instructionStack.push(i);
								result[i] = new ProgramTuple(Ops.OP_DO, pointer);
								continue;
							}
							default -> {
								result[i] = new ProgramTuple(op.get(), token.value());
								continue;
							}
						}
					}
					throw new AssertionError("Unhandled operation: " + token.value());
				}
			}
		}
		return result;
	}
}