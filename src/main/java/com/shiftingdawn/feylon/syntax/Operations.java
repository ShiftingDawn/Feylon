package com.shiftingdawn.feylon.syntax;

import java.util.Arrays;
import java.util.Optional;

public enum Operations {

	NOOP("noop"),
	POP("pop"),
	DUP("dup"),

	SYSCALL3("syscall3"),

	MEM("mem"),
	MEMSET("memset"),
	MEMGET("memget"),

	PRINT("print");

	private final String textValue;

	Operations(final String textValue) {
		this.textValue = textValue;
	}

	public static Optional<Operations> getByText(final String str) {
		return Arrays.stream(Operations.values())
				.filter(operation -> operation.textValue.equals(str))
				.findFirst();
	}
}