package com.shiftingdawn.stacklang;

import com.shiftingdawn.stacklang.instruction.*;

import java.util.Optional;

public final class Main {

	public static void main(final String[] args) {
		final Stack stack = new Stack();
		final String program = "1 1 + 2 = if 0 if 69 . else 6969 . end 420 . end";

		final ProgramTuple[] parsed = Main.parseProgram(program);
		final Instruction[] instructions = Main.makeInstructions(parsed);

		Main.simulate(stack, instructions);
	}

	public static void simulate(final Stack stack, final Instruction[] program) {
		final int[] pointer = {0};
		while (pointer[0] < program.length) {
			final Instruction instruction = program[pointer[0]++];
			if (instruction instanceof final JumpInstruction jumpInstruction) {
				jumpInstruction.apply(p -> pointer[0] = p, stack);
			} else {
				instruction.apply(stack);
			}
		}
	}

	public static ProgramTuple[] parseProgram(final String program) {
		final Stack instructionStack = new Stack();
		final String[] words = program.split("[\\s\\n]+");
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
					case OP_END -> {
						final int pointer = instructionStack.pop();
						final ProgramTuple tuple = result[pointer];
						if (tuple.op != Ops.OP_IF && tuple.op != Ops.OP_ELSE) {
							throw new AssertionError(Ops.OP_END + " operation refers to illegal operation " + tuple.op);
						}
						tuple.data = i;
						result[i] = new ProgramTuple(Ops.OP_END, i + 1);
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

	public static Instruction[] makeInstructions(final ProgramTuple[] program) {
		final Instruction[] result = new Instruction[program.length];
		for (int pointer = 0; pointer < program.length; ++pointer) {
			final Instruction ins = switch (program[pointer].op) {
				case NOOP -> new NoopInstruction();
				case OP_PUSH -> new PushInstruction((Integer) program[pointer].data);
				case OP_POP -> new PopInstruction();
				case OP_DUP -> new DupInstruction();
				case OP_PRINT -> new PrintInstruction();
				case OP_EQUALS -> new EqualsInstruction();
				case OP_ADD -> new AddInstruction();
				case OP_SUBTRACT -> new SubtractInstruction();
				case OP_MULTIPLY -> new MultiplyInstruction();
				case OP_DIVIDE -> new DivideInstruction();
				case OP_IF -> new IfInstruction((Integer) program[pointer].data);
				case OP_ELSE -> new ElseInstruction((Integer) program[pointer].data);
				case OP_END -> new EndInstruction((Integer) program[pointer].data);
			};
			result[pointer] = ins;
		}
		return result;
	}
}