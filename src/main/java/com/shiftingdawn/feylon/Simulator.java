package com.shiftingdawn.feylon;

import com.shiftingdawn.feylon.syntax.Program;

public class Simulator {

	private final Stack dataStack;
	private final Stack returnStack;
	private final Memory memory;
	private int currentInstruction;

	public Simulator(final Stack dataStack, final Stack returnStack, final Memory memory) {
		this.dataStack = dataStack;
		this.returnStack = returnStack;
		this.memory = memory;
	}

	public Simulator() {
		this(new Stack(), new Stack(), new Memory());
	}

	public void execute(final Program program) {
		final Instruction[] instructions = program.instructions();
		while (this.currentInstruction < instructions.length) {
			instructions[this.currentInstruction++].apply(this::jump, this.dataStack, this.returnStack, this.memory);
		}
	}

	public void jump(final int pointer) {
		this.currentInstruction = pointer;
	}
}