package com.shiftingdawn.feylon;

import com.shiftingdawn.feylon.ins.*;
import com.shiftingdawn.feylon.ins.jump.*;
import com.shiftingdawn.feylon.ins.mem.MemGetInstruction;
import com.shiftingdawn.feylon.ins.mem.MemSetInstruction;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

public class Parser {

	public static Instruction[] parse(final Collection<String> program) {
		return Parser.makeInstructions(Parser.makeTuples(program));
	}

	private static ProgramTuple[] makeTuples(final Collection<String> program) {
		final Stack instructionStack = new Stack();
		final String[] words = program.stream()
				.map(line -> line.split("//", 2)[0])
				.flatMap(line -> Arrays.stream(line.split("\\s+")))
				.toArray(String[]::new);
		final ProgramTuple[] result = new ProgramTuple[words.length];
		for (int i = 0; i < words.length; ++i) {
			final String word = words[i];
			try {
				final int x = Integer.parseInt(word);
				result[i] = new ProgramTuple(Ops.OP_PUSH, x);
				continue;
			} catch (final NumberFormatException ignored) {
			}
			final Optional<Ops> op = Ops.getBySymbol(word);
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
						result[i] = new ProgramTuple(op.get(), word);
						continue;
					}
				}
			}
			throw new AssertionError("Unhandled operation: " + word);
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