package com.shiftingdawn.stacklang;

public final class Main {

	public static void main(final String[] args) {
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
}