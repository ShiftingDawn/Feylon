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
		int pointer = 0;
		while (pointer < program.length) {
			final Instruction instruction = program[pointer];
			if (instruction instanceof ConditionalJumpInstruction) {
				final int x = stack.pop();
				if (x == 0) {
					instruction.apply(stack);
					pointer = stack.pop();
				} else {
					++pointer;
				}
			} else if (instruction instanceof JumpInstruction) {
				instruction.apply(stack);
				pointer = stack.pop();
			} else {
				instruction.apply(stack);
				++pointer;
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
					case OP_END -> {
						final int pointer = instructionStack.pop();
						final ProgramTuple tuple = result[pointer];
						if (tuple.op != Ops.OP_IF) {
							throw new AssertionError(Ops.OP_END + " operation refers to illegal operation " + tuple.op);
						}
						tuple.data = i;
						result[i] = new ProgramTuple(Ops.NOOP, null);
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
				case OP_PRINT -> new PrintInstruction();
				case OP_EQUALS -> new EqualsInstruction();
				case OP_ADD -> new AddInstruction();
				case OP_SUBTRACT -> new SubtractInstruction();
				case OP_MULTIPLY -> new MultiplyInstruction();
				case OP_DIVIDE -> new DivideInstruction();
				case OP_IF -> new ConditionalJumpInstruction((Integer) program[pointer].data);
				case OP_END -> throw new AssertionError("If you see this, the parsing of the program is broken");
			};
			result[pointer] = ins;
		}
		return result;
	}
}