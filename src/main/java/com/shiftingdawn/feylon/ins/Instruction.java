package com.shiftingdawn.feylon.ins;

import com.shiftingdawn.feylon.Memory;
import com.shiftingdawn.feylon.Stack;

import java.util.function.IntConsumer;

@FunctionalInterface
public interface Instruction {

	void apply(IntConsumer jump, Stack data, Stack returnStack, Memory memory);
}