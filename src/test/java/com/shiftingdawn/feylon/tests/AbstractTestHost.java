package com.shiftingdawn.feylon.tests;

import com.shiftingdawn.feylon.Memory;
import com.shiftingdawn.feylon.Simulator;
import com.shiftingdawn.feylon.Stack;
import com.shiftingdawn.feylon.StackUnderflowError;
import com.shiftingdawn.feylon.syntax.Compiler;
import com.shiftingdawn.feylon.syntax.Program;
import org.junit.jupiter.api.Assertions;

import java.util.List;

public abstract class AbstractTestHost {

	protected Stack dataStack;
	protected Stack returnStack;
	protected Memory memory;

	public void run(final String src) {
		this.dataStack = new Stack();
		this.returnStack = new Stack();
		this.memory = new Memory();
		final Program program = Compiler.compile("<generated>", List.of(src));
		new Simulator(this.dataStack, this.returnStack, this.memory).execute(program);
	}

	public void assertStack(final int value) {
		Assertions.assertEquals(value, this.dataStack.pop());
	}

	public void assertReturnTo(final int value) {
		Assertions.assertEquals(value, this.returnStack.pop());
	}

	public void assertMemory(final int pointer, final int value) {
		Assertions.assertEquals(value, this.memory.get(pointer));
	}

	public void assertStackEmpty() {
		Assertions.assertThrows(StackUnderflowError.class, this.dataStack::pop);
	}
}
