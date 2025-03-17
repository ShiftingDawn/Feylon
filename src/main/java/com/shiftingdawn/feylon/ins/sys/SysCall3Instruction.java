package com.shiftingdawn.feylon.ins.sys;

import com.shiftingdawn.feylon.*;

import java.util.function.IntConsumer;

public class SysCall3Instruction implements Instruction {

	@Override
	public void apply(final IntConsumer jump, final Stack data, final Stack returnStack, final Memory memory) {
		final int call = data.pop();

		final SysCalls sysCall = SysCalls.getByIndex(call)
				.orElseThrow(() -> new AssertionError("SysCall " + call + " is not implemented"));

		if (sysCall.getArgCount() != 3) {
			throw new AssertionError("SysCall " + call + " required 3 arguments");
		}

		Register.RDX.set(data.pop());
		Register.RSI.set(data.pop());
		Register.RDI.set(data.pop());
		Register.RAX.set(call);

		sysCall.instantiate().apply(memory);
	}
}
