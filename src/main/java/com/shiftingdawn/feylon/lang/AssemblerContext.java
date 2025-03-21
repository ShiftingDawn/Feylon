package com.shiftingdawn.feylon.lang;

import com.shiftingdawn.feylon.OrderedList;
import com.shiftingdawn.feylon.ins.Instruction;

final class AssemblerContext {

	public final OrderedList<Instruction> result = new OrderedList<>();
}