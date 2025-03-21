package com.shiftingdawn.feylon.syscall;

import com.shiftingdawn.feylon.Memory;

public interface SysCall {

	void apply(Memory memory);
}