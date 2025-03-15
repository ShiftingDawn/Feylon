package com.shiftingdawn.stacklang;

import com.shiftingdawn.stacklang.instruction.*;

public final class Main {

	public static void main(final String[] args) {
		final Stack stack = new Stack();
		final String program = "1 2 + 3 * 9 = .";

		final ProgramTuple[] parsed = Main.parseProgram(program);
		final Instruction[] instructions = Main.makeInstructions(parsed);

		for (final Instruction instruction : instructions) {
			instruction.apply(stack, null);
		}
	}

	public static ProgramTuple[] parseProgram(final String program) {
		final String[] words = program.split("[\\s\\n]+");
		final ProgramTuple[] result = new ProgramTuple[words.length];
		for (int i = 0; i < words.length; ++i) {
			final String word = words[i];
			try {
				final int x = Integer.parseInt(word);
				result[i] = new ProgramTuple(Ops.OP_PUSH, x);
			} catch (final NumberFormatException ignored) {
				final Ops op = Ops.getBySymbol(word).orElseThrow(() -> new IllegalArgumentException("Unknown operation: " + word));
				result[i] = new ProgramTuple(op, word);
			}
		}
		return result;
	}

	public static Instruction[] makeInstructions(final ProgramTuple[] program) {
		final Instruction[] result = new Instruction[program.length];
		int pointer = 0;
		while (pointer < program.length) {
			final Instruction ins = switch (program[pointer].op) {
				case OP_PUSH -> new PushInstruction((Integer) program[pointer].data);
				case OP_PRINT -> new PrintInstruction();
				case OP_EQUALS -> new EqualsInstruction();
				case OP_ADD -> new AddInstruction();
				case OP_SUBTRACT -> new SubtractInstruction();
				case OP_MULTIPLY -> new MultiplyInstruction();
				case OP_DIVIDE -> new DivideInstruction();
			};
			result[pointer] = ins;
			++pointer;
		}
		return result;
	}
}