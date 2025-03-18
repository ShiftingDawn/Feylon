package com.shiftingdawn.feylon.syntax;

import com.shiftingdawn.feylon.OrderedList;

import java.util.AbstractMap;
import java.util.Map;

public class Prevaluator {

	public static Map.Entry<Integer, DataType> evaluateConstant(final TokenPos pos, final CompilerContext ctx, final OrderedList<Token> tokens) {
		final OrderedList<Map.Entry<Integer, DataType>> stack = new OrderedList<>();
		Outer:
		while (!tokens.isEmpty()) {
			final Token token = tokens.pop();
			switch (token.type()) {
				case KEYWORD -> {
					if (token.operand() == Keyword.END) {
						break Outer;
					} else {
						throw new CompilerException(token.pos(), "Encountered illegal keyword '%s' when evaluating constant".formatted(token.operand()));
					}
				}
				case INTEGER -> {
					assert token.operand() instanceof Integer;
					stack.append(new AbstractMap.SimpleEntry<>((int) token.operand(), DataType.INTEGER));
				}
				default -> throw new CompilerException(token.pos(), "Encountered illegal '%s' token '%s' when evaluating constant".formatted(token.type(), token.txt()));
			}
		}
		if (stack.size() != 1) {
			throw new CompilerException(pos, "The value of a constant should evaluate to a single number!");
		}
		return stack.pop();
	}
}