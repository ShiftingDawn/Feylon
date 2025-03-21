package com.shiftingdawn.feylon.syscall;

public enum Register {

	RAX, RDI, RSI, RDX, R10, R8, R9;

	private long data = 0;

	public void set(final long data) {
		this.data = data;
	}

	public long get() {
		return this.data;
	}
}