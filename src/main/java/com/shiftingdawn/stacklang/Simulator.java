package com.shiftingdawn.stacklang;

public class Simulator {

	private final Stack stack;
	private final Memory memory;

	public Simulator(final Stack stack, final Memory memory) {
		this.stack = stack;
		this.memory = memory;
	}

	public Simulator() {
		this(new Stack(), new Memory());
	}

	public void execute(final Instruction[] program) {
		final int[] pointer = {0};
		while (pointer[0] < program.length) {
			final Instruction instruction = program[pointer[0]++];
			if (instruction instanceof final JumpInstruction jumpInstruction) {
				jumpInstruction.apply(p -> pointer[0] = p, this.stack);
			} else if (instruction instanceof final MemoryInstruction memoryInstruction) {
				memoryInstruction.apply(this.memory, this.stack);
			} else {
				instruction.apply(this.stack);
			}
		}
	}
}