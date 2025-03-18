package com.shiftingdawn.feylon.syntax;

import java.util.Arrays;
import java.util.Optional;

public enum DataType {

	INTEGER("int"),
	POINTER("ptr");

	private final String textValue;

	DataType(final String textValue) {
		this.textValue = textValue;
	}

	public static Optional<DataType> getByText(final String str) {
		return Arrays.stream(DataType.values())
				.filter(operation -> operation.textValue.equals(str))
				.findFirst();
	}
}
