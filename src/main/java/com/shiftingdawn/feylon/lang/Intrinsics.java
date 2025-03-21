package com.shiftingdawn.feylon.lang;

public enum Intrinsics {

	ADD("+"),
	SUBTRACT("-"),
	MULTIPLY("*"),
	DIVIDE("/"),
	MODULO("%"),

	SHIFT_LEFT("<<"),
	SHIFT_RIGHT(">>"),
	BITWISE_AND("&"),
	BITWISE_OR("|"),
	BITWISE_XOR("^"),

	EQUALS("="),
	NOT_EQUALS("!="),
	LESS("<"),
	GREATER(">"),
	LESS_OR_EQUAL("<="),
	GREATER_OR_EQUAL(">="),

	DUMP("dump"),
	POP("pop"),
	DUP("dup"),
	SWAP("swap"),
	OVER("over"),
	ROT("rot"),

	STORE("store"),
	LOAD("load"),
	STORE_16("store16"),
	LOAD_16("load16"),
	STORE_32("store32"),
	LOAD_32("load32"),

	SYSCALL_0("syscall0"),
	SYSCALL_1("syscall1"),
	SYSCALL_2("syscall2"),
	SYSCALL_3("syscall3"),
	SYSCALL_4("syscall4"),
	SYSCALL_5("syscall5"),
	SYSCALL_6("syscall6");

	public final String textValue;

	Intrinsics(final String textValue) {
		this.textValue = textValue;
	}

	public static Intrinsics getByText(final String str) {
		for (final Intrinsics intrinsic : Intrinsics.values()) {
			if (intrinsic.textValue.equals(str)) {
				return intrinsic;
			}
		}
		return null;
	}
}