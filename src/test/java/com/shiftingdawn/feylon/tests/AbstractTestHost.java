package com.shiftingdawn.feylon.tests;

import com.shiftingdawn.feylon.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;

import java.util.List;

public abstract class AbstractTestHost {

	protected Stack stack;
	protected Memory memory;

	@BeforeEach
	public void makeStack() {
		this.stack = new Stack();
	}

	public void run(final String program) {
		this.stack = new Stack();
		this.memory = new Memory();
		final Instruction[] compiled = Parser.parse(List.of(program));
		new Simulator(this.stack, this.memory).execute(compiled);
	}

	public void assertStack(final int value) {
		Assertions.assertEquals(value, this.stack.pop());
	}

	public void assertMemory(final int pointer, final int value) {
		Assertions.assertEquals(value, this.memory.get(pointer));
	}

	public void assertStackEmpty() {
		Assertions.assertThrows(StackUnderflowError.class, this.stack::pop);
	}
}
