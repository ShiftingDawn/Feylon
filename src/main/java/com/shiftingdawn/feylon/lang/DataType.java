package com.shiftingdawn.feylon.lang;

public enum DataType {

	INT("int"),
	BOOL("bool"),
	POINTER("ptr");

	private final String textValue;

	DataType(final String textValue) {
		this.textValue = textValue;
	}

	public static DataType getByText(final String str) {
		for (final DataType dataType : DataType.values()) {
			if (dataType.textValue.equals(str)) {
				return dataType;
			}
		}
		return null;
	}
}