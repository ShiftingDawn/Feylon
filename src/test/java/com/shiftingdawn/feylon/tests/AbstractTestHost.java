package com.shiftingdawn.feylon.tests;

import com.shiftingdawn.feylon.Memory;
import com.shiftingdawn.feylon.Simulator;
import com.shiftingdawn.feylon.Stack;
import com.shiftingdawn.feylon.StackUnderflowError;
import com.shiftingdawn.feylon.lang.*;
import org.junit.jupiter.api.Assertions;

import java.util.List;

public abstract class AbstractTestHost {

	protected Stack dataStack;
	protected Stack returnStack;
	protected Memory memory;

	public void run(final String src, final int vars) {
		this.dataStack = new Stack();
		this.returnStack = new Stack();
		this.memory = new Memory();
		final AssembledProgram program = Feylon.parse(new ResolvedSources("<generated>", List.of(src)), vars);
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

	public void assertExceptionWithCode(final CompilerErrors error, final String program) {
		final CompilerException ex = Assertions.assertThrows(CompilerException.class, () -> this.run(program, 0));
		if (!error.equals(ex.code)) {
			ex.printStackTrace();
			Assertions.assertEquals(error, ex.code);
		}
	}
}
