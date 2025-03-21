package com.shiftingdawn.feylon.lang;

import com.shiftingdawn.feylon.OrderedList;

import java.util.HashMap;
import java.util.Map;

final class ParserContext {

	public final OrderedList<Token> result = new OrderedList<>();
	public final Map<String, FunctionSignature> functions = new HashMap<>();
	public final Map<String, ConstDef> constants = new HashMap<>();
}