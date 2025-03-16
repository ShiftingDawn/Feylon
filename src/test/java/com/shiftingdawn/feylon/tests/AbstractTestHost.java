package com.shiftingdawn.feylon.tests;

import com.shiftingdawn.feylon.Memory;
import com.shiftingdawn.feylon.Simulator;
import com.shiftingdawn.feylon.Stack;
import com.shiftingdawn.feylon.StackUnderflowError;
import com.shiftingdawn.feylon.syntax.Compiler;
import com.shiftingdawn.feylon.syntax.Program;
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

	public void run(final String src) {
		this.stack = new Stack();
		this.memory = new Memory();
		final Program program = Compiler.compile("<generated>", List.of(src));
		new Simulator(this.stack, this.memory).execute(program);
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
