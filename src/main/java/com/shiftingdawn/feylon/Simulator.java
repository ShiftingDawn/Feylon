package com.shiftingdawn.feylon;

import com.shiftingdawn.feylon.ins.Instruction;
import com.shiftingdawn.feylon.lang.AssembledProgram;

public class Simulator {

	private final Stack dataStack;
	private final Stack returnStack;
	private final Memory memory;
	private final AssembledProgram program;
	private int currentInstruction;

	public Simulator(final Stack dataStack, final Stack returnStack, final Memory memory, final AssembledProgram program) {
		this.dataStack = dataStack;
		this.returnStack = returnStack;
		this.memory = memory;
		this.program = program;
	}

	public Simulator(final AssembledProgram program) {
		this(new Stack(), new Stack(), new Memory(program.memorySize()), program);
	}

	public void execute() {
		while (this.currentInstruction < this.program.instructions().length) {
			final Instruction instruction = this.program.instructions()[this.currentInstruction++];
			if (instruction == null) {
				continue;
			}
			instruction.apply(this::jump, this.dataStack, this.returnStack, this.memory);
		}
	}

	public void jump(final int pointer) {
		this.currentInstruction = pointer;
	}
}