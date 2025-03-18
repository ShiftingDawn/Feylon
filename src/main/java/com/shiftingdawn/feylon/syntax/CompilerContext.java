package com.shiftingdawn.feylon.syntax;

import com.shiftingdawn.feylon.OrderedList;

import java.util.HashMap;
import java.util.Map;

public class CompilerContext {

	public final OrderedList<InstructionSource> instructions = new OrderedList<>();
	public final Map<String, FunctionDef> functions = new HashMap<>();
	public final Map<String, ConstantDef> constants = new HashMap<>();
}