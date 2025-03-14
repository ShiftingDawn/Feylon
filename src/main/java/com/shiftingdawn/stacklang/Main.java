package com.shiftingdawn.stacklang;

import java.util.Arrays;

public final class Main {

	public static void main(String[] args) {
		final Stack stack = new Stack();
		String program = "1 2 + .";
		Object[] parsed = parseProgram(program);
		System.out.println(Arrays.toString(parsed));
		for (Object symbol : parsed) {
			switch (symbol) {
				case Double d -> stack.push(d);
				case Op op -> Operator.apply(stack, op);
				default -> throw new IllegalStateException("Unexpected value: " + symbol);
			}
		}
	}

	public static Object[] parseProgram(String program) {
		String[] words = program.split("[\\s\\n]+");
		Object[] result = new Object[words.length];
		for (int i = 0; i < words.length; ++i) {
			final String word = words[i];
			try {
				double d = Double.parseDouble(word);
				result[i] = d;
			} catch (NumberFormatException ignored) {
				Op op = Op.parse(word).orElseThrow(() -> new IllegalArgumentException("Unknown operation: " + word));
				result[i] = op;
			}
		}
		return result;
	}
}