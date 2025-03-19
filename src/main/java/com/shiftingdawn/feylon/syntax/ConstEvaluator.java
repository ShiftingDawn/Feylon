package com.shiftingdawn.feylon.syntax;

import com.shiftingdawn.feylon.OrderedList;
import com.shiftingdawn.feylon.lang.Intrinsics;
import com.shiftingdawn.feylon.lang.Keywords;
import com.shiftingdawn.feylon.lang.RawToken;
import com.shiftingdawn.feylon.lang.TokenPos;

import java.util.AbstractMap;
import java.util.Map;

public class ConstEvaluator {

	public static Map.Entry<Integer, DataType> evaluateConstant(final TokenPos pos, final CompilerContext ctx, final OrderedList<RawToken> tokens) {
		final OrderedList<Map.Entry<Integer, DataType>> stack = new OrderedList<>();
		Outer:
		while (!tokens.isEmpty()) {
			final RawToken token = tokens.pop();
			switch (token.type()) {
				case KEYWORD -> {
					if (token.operand() == Keywords.END) {
						break Outer;
					} else {
						throw new CompilerException(token.pos(), CompilerErrors.ILLEGAL_CONSTANT_VALUE, "Encountered illegal keyword '%s' when evaluating constant".formatted(token.operand()));
					}
				}
				case INT -> {
					assert token.operand() instanceof Integer;
					stack.append(new AbstractMap.SimpleEntry<>((int) token.operand(), DataType.INTEGER));
				}
				case INTRINSIC -> {
					final Map.Entry<Integer, DataType> a = stack.pop();
					final Map.Entry<Integer, DataType> b = stack.pop();
					if (a.getValue() != DataType.INTEGER) {
						throw new CompilerException(token.pos(), CompilerErrors.INVALID_DATA, "Encountered invalid data type '%s' when evaluating constant".formatted(a.getValue()));
					} else if (b.getValue() != DataType.INTEGER) {
						throw new CompilerException(token.pos(), CompilerErrors.INVALID_DATA, "Encountered invalid data type '%s' when evaluating constant".formatted(b.getValue()));
					}
					switch ((Intrinsics) token.operand()) {
						case ADD -> stack.append(new AbstractMap.SimpleEntry<>(a.getKey() + b.getKey(), DataType.INTEGER));
						case SUBTRACT -> stack.append(new AbstractMap.SimpleEntry<>(b.getKey() - a.getKey(), DataType.INTEGER));
						case MULTIPLY -> stack.append(new AbstractMap.SimpleEntry<>(a.getKey() * b.getKey(), DataType.INTEGER));

						default -> throw new CompilerException(token.pos(), CompilerErrors.ILLEGAL_CONSTANT_VALUE,
								"Encountered illegal intrinsic '%s' when evaluating constant".formatted(token.operand()));
					}
				}
				default -> throw new CompilerException(token.pos(), CompilerErrors.ILLEGAL_CONSTANT_VALUE,
						"Encountered illegal '%s' token '%s' when evaluating constant".formatted(token.type(), token.txt()));
			}
		}
		if (stack.size() != 1) {
			throw new CompilerException(pos, CompilerErrors.ILLEGAL_CONSTANT_VALUE, "The value of a constant should evaluate to a single number");
		}
		return stack.pop();
	}
}