package com.shiftingdawn.stacklang;

public interface Instruction {

	void apply(Stack stack, Object token);
}