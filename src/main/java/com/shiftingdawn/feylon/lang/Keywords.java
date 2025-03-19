package com.shiftingdawn.feylon.lang;

import java.util.Arrays;
import java.util.Optional;

public enum Keywords {

	IMPORT("import"),
	FUNCTION("function"),
	CONST("const"),
	END("end"),
	IF("if"),
	ELSE("else"),
	WHILE("while"),
	DO("do"),

	TYPE_INT("int", false),
	TYPE_BOOL("bool", false),
	TYPE_STR("str", false),
	TYPE_PTR("ptr", false);

	public final String textValue;
	public final boolean hasInstruction;

	Keywords(final String textValue, final boolean hasInstruction) {
		this.textValue = textValue;
		this.hasInstruction = hasInstruction;
	}

	Keywords(final String textValue) {
		this(textValue, true);
	}

	public static Optional<Keywords> getByText(final String str) {
		return Arrays.stream(Keywords.values())
				.filter(intrinsic -> intrinsic.textValue.equals(str))
				.findFirst();
	}
}