package com.shiftingdawn.feylon;

import java.util.function.IntConsumer;

public interface JumpInstruction extends Instruction {

	@Override
	default void apply(final Stack stack) {
		throw new AssertionError("Use apply(IntConsumer, Stack) instead!");
	}

	void apply(IntConsumer jump, Stack stack);
}