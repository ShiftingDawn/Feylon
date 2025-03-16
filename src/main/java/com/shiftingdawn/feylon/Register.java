package com.shiftingdawn.feylon;

public enum Register {

	RAX, RDI, RSI, RDX;

	private long data = 0;

	public void set(final long data) {
		this.data = data;
	}

	public long get() {
		return this.data;
	}
}