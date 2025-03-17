package com.shiftingdawn.feylon;

import java.util.function.IntConsumer;

public interface Instruction {

	void apply(IntConsumer jump, Stack data, Stack returnStack, Memory memory);
}