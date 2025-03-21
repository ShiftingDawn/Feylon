package com.shiftingdawn.feylon.lang;

public enum TokenType {

	INT,
	BOOL,
	STRING,

	INTRINSIC,
	CONST,
	CONST_REF,
	MEMORY,
	MEMORY_REF,

	FUNCTION,
	FUNCTION_CALL,
	END,
	IF,
	ELSE,
	WHILE,
	DO,
}