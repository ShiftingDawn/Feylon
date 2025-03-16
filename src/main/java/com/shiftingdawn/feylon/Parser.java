package com.shiftingdawn.feylon;

import com.shiftingdawn.feylon.ins.*;
import com.shiftingdawn.feylon.ins.jump.*;
import com.shiftingdawn.feylon.ins.mem.MemGetInstruction;
import com.shiftingdawn.feylon.ins.mem.MemSetInstruction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class Parser {

	public static Instruction[] parse(final Collection<String> program) {
		return Parser.makeInstructions(Parser.makeTuples(program));
	}

	private static int findPosition(final String line, int start, final Predicate<Character> predicate) {
		while (start < line.length() && !predicate.test(line.charAt(start))) {
			++start;
		}
		return start;
	}

	private record LexedLineToken(int pos, LexedTokenType type, Object value) {
	}

	private enum LexedTokenType {
		INT, INSTRUCTION, STRING;
	}


	private static LexedLineToken[] lexLine(final String line) {
		final List<LexedLineToken> result = new ArrayList<>();
		int pos = Parser.findPosition(line, 0, x -> x != ' ');
		while (pos < line.length()) {
			if (line.charAt(pos) == '"') {
				final int endPos = Parser.findPosition(line, pos + 1, x -> x == '"');
				assert line.charAt(endPos) == '"';
				final String tokenText = line.substring(pos + 1, endPos);
				result.add(new LexedLineToken(pos, LexedTokenType.STRING, tokenText));
				pos = Parser.findPosition(line, endPos + 2, x -> x != ' ');
			} else {
				final int endPos = Parser.findPosition(line, pos, x -> x == ' ');
				final String tokenText = line.substring(pos, endPos);
				try {
					result.add(new LexedLineToken(pos, LexedTokenType.INT, Integer.parseInt(tokenText)));
				} catch (final NumberFormatException ignored) {
					result.add(new LexedLineToken(pos, LexedTokenType.INSTRUCTION, tokenText));
				}
				pos = Parser.findPosition(line, endPos, x -> x != ' ');
			}
		}
		return result.toArray(LexedLineToken[]::new);
	}

	private record LexedLinePosition(String file, int lineNr, int charPos) {
	}

	private record LexedLine(LexedTokenType type, LexedLinePosition pos, Object value) {
	}

	public static LexedLine[] lexLines(final String fileName, final String[] lines) {
		final ArrayList<LexedLine> result = new ArrayList<>();
		for (int row = 0; row < lines.length; ++row) {
			final LexedLineToken[] tokens = Parser.lexLine(lines[row].split("//", 2)[0]);
			for (final LexedLineToken token : tokens) {
				result.add(new LexedLine(token.type, new LexedLinePosition(fileName, row + 1, token.pos + 1), token.value));
			}
		}
		return result.toArray(new LexedLine[0]);
	}

	private static ProgramTuple[] makeTuples(final Collection<String> program) {
		final LexedLine[] tokenData = Parser.lexLines(null, program.toArray(new String[0]));
		final Stack instructionStack = new Stack();
		final ProgramTuple[] result = new ProgramTuple[tokenData.length];
		for (int i = 0; i < tokenData.length; ++i) {
			final LexedLine token = tokenData[i];
			switch (token.type) {
				case INT -> result[i] = new ProgramTuple(Ops.OP_PUSH, token.value);
				case INSTRUCTION -> {
					final Optional<Ops> op = Ops.getBySymbol((String) token.value);
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
								result[i] = new ProgramTuple(op.get(), token.value);
								continue;
							}
						}
					}
					throw new AssertionError("Unhandled operation: " + token.value);
				}
			}
		}
		return result;
	}

	private static Instruction[] makeInstructions(final ProgramTuple[] program) {
		final Instruction[] result = new Instruction[program.length];
		for (int pointer = 0; pointer < program.length; ++pointer) {
			final Instruction ins = switch (program[pointer].op) {
				case NOOP -> new NoopInstruction();
				case OP_PUSH -> new PushInstruction((Integer) program[pointer].data);
				case OP_POP -> new PopInstruction();
				case OP_DUP -> new DupInstruction();
				case OP_MEM -> new NoopInstruction();
				case OP_MEM_SET -> new MemSetInstruction();
				case OP_MEM_GET -> new MemGetInstruction();
				case OP_PRINT -> new PrintInstruction();
				case OP_EQUALS -> new EqualsInstruction();
				case OP_NOT_EQUALS -> new NotEqualsInstruction();
				case OP_LESS -> new LessInstruction();
				case OP_GREATER -> new GreaterInstruction();
				case OP_LESS_EQUAL -> new LessEqualInstruction();
				case OP_GREATER_EQUAL -> new GreaterEqualInstruction();
				case OP_ADD -> new AddInstruction();
				case OP_SUBTRACT -> new SubtractInstruction();
				case OP_MULTIPLY -> new MultiplyInstruction();
				case OP_DIVIDE -> new DivideInstruction();
				case OP_MOD -> new ModInstruction();
				case OP_END -> new EndInstruction((Integer) program[pointer].data);
				case OP_IF -> new IfInstruction((Integer) program[pointer].data);
				case OP_ELSE -> new ElseInstruction((Integer) program[pointer].data);
				case OP_WHILE -> new WhileInstruction();
				case OP_DO -> new DoInstruction((Integer) program[pointer].data);
			};
			result[pointer] = ins;
		}
		return result;
	}
}