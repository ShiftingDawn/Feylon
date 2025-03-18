package com.shiftingdawn.feylon.syntax;

public enum InstructionType {

	PUSH_INT,
	PUSH_STRING,

	INTRINSIC,

	JUMP,
	CALL,
	RETURN,

	IF,
	ELSE,
	WHILE,
	DO,
	FUNCTION,

	INSTRUCTION
}