package com.shiftingdawn.stacklang;

public final class Main {

	public static void main(final String[] args) {
		final Stack stack = new Stack();
		final String program = "1 2 + 3 * 9 = .";
		final Instruction[] parsed = Main.parseProgram(program);
		for (final Instruction instruction : parsed) {
			final Tuple<Ops, Object> op = instruction.op();
			op.a().apply(stack, op.b());
		}
	}

	public static Instruction[] parseProgram(final String program) {
		final String[] words = program.split("[\\s\\n]+");
		final Instruction[] result = new Instruction[words.length];
		for (int i = 0; i < words.length; ++i) {
			final String word = words[i];
			try {
				final double d = Double.parseDouble(word);
				result[i] = new Instruction(new Tuple<>(Ops.OP_PUSH, d), i, i + 1);
			} catch (final NumberFormatException ignored) {
				final Ops op = Ops.parse(word).orElseThrow(() -> new IllegalArgumentException("Unknown operation: " + word));
				result[i] = new Instruction(new Tuple<>(op, word), i, i + 1);
			}
		}
		return result;
	}
}