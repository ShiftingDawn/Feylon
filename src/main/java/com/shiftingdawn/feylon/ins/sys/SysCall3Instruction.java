package com.shiftingdawn.feylon.ins.sys;

import com.shiftingdawn.feylon.*;

public class SysCall3Instruction implements MemoryInstruction {

	@Override
	public void apply(final Memory memory, final Stack stack) {
		final int call = stack.pop();

		final SysCalls sysCall = SysCalls.getByIndex(call)
				.orElseThrow(() -> new AssertionError("SysCall " + call + " is not implemented"));

		if (sysCall.getArgCount() != 3) {
			throw new AssertionError("SysCall " + call + " required 3 arguments");
		}

		Register.RDX.set(stack.pop());
		Register.RSI.set(stack.pop());
		Register.RDI.set(stack.pop());
		Register.RAX.set(call);

		sysCall.instantiate().apply(memory);
	}
}
