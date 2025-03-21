package com.shiftingdawn.feylon.lang;

import com.shiftingdawn.feylon.OrderedList;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

final class LinkerContext {

	public final Map<String, FunctionRef> functions = new HashMap<>();
	public final OrderedList<Token> tokens = new OrderedList<>();
	public final OrderedList<LinkedToken> result = new OrderedList<>();
	public final Stack<Integer> callStack = new Stack<>();
	int memSize;
	int pointer = 0;

	public LinkerContext(final ParserContext sources) {
		this.memSize = sources.totalMemory;
		this.tokens.addAll(sources.result);
		this.tokens.reverse();
		sources.functions.forEach((funcName, funcSignature) -> {
			this.functions.put(funcName, new FunctionRef(funcSignature.inputs(), funcSignature.outputs()));
		});
	}

	public void add(final ParserContext sources) {
		this.memSize += sources.totalMemory;
		this.tokens.addAll(new OrderedList<>(sources.result).reverse());
		sources.functions.forEach((funcName, funcSignature) -> {
			this.functions.put(funcName, new FunctionRef(funcSignature.inputs(), funcSignature.outputs()));
		});
	}
}