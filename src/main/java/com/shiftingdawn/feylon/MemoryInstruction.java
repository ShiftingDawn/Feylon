package com.shiftingdawn.feylon;

public interface MemoryInstruction extends Instruction {

	@Override
	default void apply(final Stack stack) {
		throw new AssertionError("Use apply(Memory, Stack) instead!");
	}

	void apply(Memory memory, Stack stack);
}