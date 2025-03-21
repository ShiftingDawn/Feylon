package com.shiftingdawn.feylon.lang;

import com.shiftingdawn.feylon.Instruction;
import com.shiftingdawn.feylon.OrderedList;

final class AssemblerContext {

	public final OrderedList<Instruction> result = new OrderedList<>();
}