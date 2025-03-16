package com.shiftingdawn.feylon;

public class ProgramTuple {

	public final Ops op;
	public Object data;

	public ProgramTuple(final Ops op, final Object data) {
		this.op = op;
		this.data = data;
	}
}
