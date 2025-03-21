package com.shiftingdawn.feylon.lang;

import com.shiftingdawn.feylon.ins.Instruction;

public record AssembledProgram(Instruction[] instructions, int memorySize) {
}
