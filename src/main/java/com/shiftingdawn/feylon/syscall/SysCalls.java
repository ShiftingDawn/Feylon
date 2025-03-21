package com.shiftingdawn.feylon.syscall;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Supplier;

public enum SysCalls {

	WRITE(1, 3, WriteSysCall::new);

	private final int index;
	private final int argc;
	private final Supplier<? extends SysCall> factory;

	SysCalls(final int index, final int argc, final Supplier<? extends SysCall> factory) {
		this.index = index;
		this.argc = argc;
		this.factory = factory;
	}

	public int getArgCount() {
		return this.argc;
	}

	public SysCall instantiate() {
		return this.factory.get();
	}

	public static Optional<SysCalls> getByIndex(final int index) {
		return Arrays.stream(SysCalls.values()).filter(call -> call.index == index).findFirst();
	}
}