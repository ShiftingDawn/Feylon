package com.shiftingdawn.feylon.ins;

import com.shiftingdawn.feylon.Memory;
import com.shiftingdawn.feylon.Stack;
import com.shiftingdawn.feylon.syscall.Register;
import com.shiftingdawn.feylon.syscall.SysCalls;

import java.util.function.IntConsumer;

public class SystemInstructions {

	public static void syscall0(final IntConsumer jump, final Stack data, final Stack returnStack, final Memory memory) {
		final int call = data.pop();
		final SysCalls sysCall = SysCalls.getByIndex(call).orElseThrow(() -> new AssertionError("SysCall '%s' is not implemented".formatted(call)));
		if (sysCall.getArgCount() != 0) {
			throw new AssertionError("SysCall '%s' required 0 arguments".formatted(call));
		}
		Register.RAX.set(call);
		sysCall.instantiate().apply(memory);
	}

	public static void syscall1(final IntConsumer jump, final Stack data, final Stack returnStack, final Memory memory) {
		final int call = data.pop();
		final SysCalls sysCall = SysCalls.getByIndex(call).orElseThrow(() -> new AssertionError("SysCall '%s' is not implemented".formatted(call)));
		if (sysCall.getArgCount() != 1) {
			throw new AssertionError("SysCall '%s' required 1 argument".formatted(call));
		}
		Register.RAX.set(call);
		Register.RDI.set(data.pop());
		sysCall.instantiate().apply(memory);
	}

	public static void syscall2(final IntConsumer jump, final Stack data, final Stack returnStack, final Memory memory) {
		final int call = data.pop();
		final SysCalls sysCall = SysCalls.getByIndex(call).orElseThrow(() -> new AssertionError("SysCall '%s' is not implemented".formatted(call)));
		if (sysCall.getArgCount() != 2) {
			throw new AssertionError("SysCall '%s' required 2 arguments".formatted(call));
		}
		Register.RAX.set(call);
		Register.RDI.set(data.pop());
		Register.RSI.set(data.pop());
		sysCall.instantiate().apply(memory);
	}

	public static void syscall3(final IntConsumer jump, final Stack data, final Stack returnStack, final Memory memory) {
		final int call = data.pop();
		final SysCalls sysCall = SysCalls.getByIndex(call).orElseThrow(() -> new AssertionError("SysCall '%s' is not implemented".formatted(call)));
		if (sysCall.getArgCount() != 3) {
			throw new AssertionError("SysCall '%s' required 3 arguments".formatted(call));
		}
		Register.RAX.set(call);
		Register.RDI.set(data.pop());
		Register.RSI.set(data.pop());
		Register.RDX.set(data.pop());
		sysCall.instantiate().apply(memory);
	}

	public static void syscall4(final IntConsumer jump, final Stack data, final Stack returnStack, final Memory memory) {
		final int call = data.pop();
		final SysCalls sysCall = SysCalls.getByIndex(call).orElseThrow(() -> new AssertionError("SysCall '%s' is not implemented".formatted(call)));
		if (sysCall.getArgCount() != 4) {
			throw new AssertionError("SysCall '%s' required 4 arguments".formatted(call));
		}
		Register.RAX.set(call);
		Register.RDI.set(data.pop());
		Register.RSI.set(data.pop());
		Register.RDX.set(data.pop());
		Register.R10.set(data.pop());
		sysCall.instantiate().apply(memory);
	}

	public static void syscall5(final IntConsumer jump, final Stack data, final Stack returnStack, final Memory memory) {
		final int call = data.pop();
		final SysCalls sysCall = SysCalls.getByIndex(call).orElseThrow(() -> new AssertionError("SysCall '%s' is not implemented".formatted(call)));
		if (sysCall.getArgCount() != 5) {
			throw new AssertionError("SysCall '%s' required 5 arguments".formatted(call));
		}
		Register.RAX.set(call);
		Register.RDI.set(data.pop());
		Register.RSI.set(data.pop());
		Register.RDX.set(data.pop());
		Register.R10.set(data.pop());
		Register.R8.set(data.pop());
		sysCall.instantiate().apply(memory);
	}

	public static void syscall6(final IntConsumer jump, final Stack data, final Stack returnStack, final Memory memory) {
		final int call = data.pop();
		final SysCalls sysCall = SysCalls.getByIndex(call).orElseThrow(() -> new AssertionError("SysCall '%s' is not implemented".formatted(call)));
		if (sysCall.getArgCount() != 6) {
			throw new AssertionError("SysCall '%s' required 6 arguments".formatted(call));
		}
		Register.RAX.set(call);
		Register.RDI.set(data.pop());
		Register.RSI.set(data.pop());
		Register.RDX.set(data.pop());
		Register.R10.set(data.pop());
		Register.R8.set(data.pop());
		Register.R9.set(data.pop());
		sysCall.instantiate().apply(memory);
	}
}
