package com.shiftingdawn.stacklang.tests;

import com.shiftingdawn.stacklang.Instruction;
import com.shiftingdawn.stacklang.Main;
import com.shiftingdawn.stacklang.Stack;
import com.shiftingdawn.stacklang.StackUnderflowError;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;

public abstract class AbstractTestHost {

	protected Stack stack;

	@BeforeEach
	public void makeStack() {
		this.stack = new Stack();
	}

	public void run(final String program) {
		this.stack = new Stack();
		final Instruction[] compiled = Main.makeInstructions(Main.parseProgram(program));
		Main.simulate(this.stack, compiled);
	}

	public void assertStack(final int value) {
		Assertions.assertEquals(value, this.stack.pop());
	}

	public void assertStackEmpty() {
		Assertions.assertThrows(StackUnderflowError.class, this.stack::pop);
	}
}
