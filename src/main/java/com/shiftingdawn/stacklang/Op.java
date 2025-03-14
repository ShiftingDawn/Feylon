package com.shiftingdawn.stacklang;

public enum Op {

	OP_ADD(2),
	OP_PRINT(1);

	public final int pops;

	Op(int pops) {
		this.pops = pops;
	}
}