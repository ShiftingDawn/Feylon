package com.shiftingdawn.feylon.syscall;

import com.shiftingdawn.feylon.Memory;
import com.shiftingdawn.feylon.Register;
import com.shiftingdawn.feylon.SysCall;

public class WriteSysCall implements SysCall {

	@Override
	public void apply(final Memory memory) {
		final int fd = (int) Register.RAX.get();
		final int buf = (int) Register.RSI.get();
		final int count = (int) Register.RDI.get();
		final String str = memory.getString(buf, count);
		switch (fd) {
			case 0 -> throw new AssertionError("Cannot currently write to STDIN");
			case 1 -> System.out.println(str);
			case 2 -> System.err.println(str);
			default -> throw new AssertionError("Can only write to STDOUT (fd=1) and STDERR (fd=2) for now");
		}
	}
}
