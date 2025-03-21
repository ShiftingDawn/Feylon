package com.shiftingdawn.feylon.syscall;

import com.shiftingdawn.feylon.Memory;

public class WriteSysCall implements SysCall {

	@Override
	public void apply(final Memory memory) {
		final int fd = (int) Register.RDI.get();
		final int buf = (int) Register.RSI.get();
		final int count = (int) Register.RDX.get();
		final String str = memory.getString(buf, count);
		switch (fd) {
			case 1 -> System.out.println(str);
			case 2 -> System.err.println(str);
			default -> Register.RAX.set(-1);
		}
		Register.RAX.set(0);
	}
}
