package com.shiftingdawn.feylon.lang;

import com.shiftingdawn.feylon.OrderedList;

import java.util.Iterator;

final class Evaluator {

	public static void evaluate(final ParserContext ctx) {
		for (final Iterator<Token> iterator = ctx.result.iterator(); iterator.hasNext(); ) {
			final Token token = iterator.next();
			if (token.type() == TokenType.CONST) {
				iterator.remove();
				if (!iterator.hasNext()) {
					throw new FeylonException(token.pos(), "Encountered incomplete constant");
				}
				final ConstValue value = Evaluator.evaluateConstant(token, ctx, iterator);
				ctx.constants.put(token.txt(), new ConstDef(token.pos(), value.type(), value.value()));
			} else if (token.type() == TokenType.MEMORY) {
				iterator.remove();
				if (!iterator.hasNext()) {
					throw new FeylonException(token.pos(), "Encountered incomplete memory definition");
				}
				final int value = Evaluator.evaluateMemory(token, ctx, iterator);
				ctx.memories.put(token.txt(), new MemoryDef(token.pos(), value));
			}
		}
		ctx.memories.values().forEach(memory -> {
			memory.offset = ctx.totalMemory;
			ctx.totalMemory += memory.memSize;
		});
	}

	private static ConstValue evaluateConstant(final Token constToken, final ParserContext ctx, final Iterator<Token> iterator) {
		final OrderedList<ConstValue> stack = new OrderedList<>();
		while (iterator.hasNext()) {
			final Token token = iterator.next();
			iterator.remove();
			if (token.type() == TokenType.END) {
				break;
			} else if (token.type() == TokenType.INT) {
				assert token.data() instanceof Integer;
				stack.append(new ConstValue(DataType.INT, (int) token.data()));
			} else if (token.type() == TokenType.INTRINSIC) {
				if (stack.size() < 2) {
					throw new FeylonException(token.pos(), "Missing data for intrinsic '%s' when evaluating constant".formatted(token.data()));
				}
				final ConstValue a = stack.pop();
				final ConstValue b = stack.pop();
				if (a.type() != DataType.INT) {
					throw new FeylonException(token.pos(), "Encountered invalid data type '%s' when evaluating constant".formatted(a.value()));
				} else if (b.type() != DataType.INT) {
					throw new FeylonException(token.pos(), "Encountered invalid data type '%s' when evaluating constant".formatted(b.value()));
				}
				switch ((Intrinsics) token.data()) {
					case ADD -> stack.append(new ConstValue(DataType.INT, a.value() + b.value()));
					case SUBTRACT -> stack.append(new ConstValue(DataType.INT, b.value() - a.value()));
					case MULTIPLY -> stack.append(new ConstValue(DataType.INT, a.value() * b.value()));

					default -> throw new FeylonException(token.pos(), "Encountered illegal intrinsic '%s' when evaluating constant".formatted(token.data()));
				}
			} else if (token.type() == TokenType.CONST_REF) {
				final ConstDef constDef = ctx.constants.get(token.txt());
				if (constDef == null) {
					throw new FeylonException(token.pos(), "Encountered constant reference '%s' before it was defined when evaluating constant".formatted(token.txt()));
				}
				stack.append(new ConstValue(constDef.dataType(), constDef.value()));
			} else {
				throw new FeylonException(token.pos(), "Encountered illegal '%s' token '%s' when evaluating constant".formatted(token.type(), token.txt()));
			}
		}
		if (stack.size() != 1) {
			throw new FeylonException(constToken.pos(), "The value of a constant should evaluate to a single number");
		}
		return stack.pop();
	}

	private static int evaluateMemory(final Token memoryToken, final ParserContext ctx, final Iterator<Token> iterator) {
		final OrderedList<Integer> stack = new OrderedList<>();
		while (iterator.hasNext()) {
			final Token token = iterator.next();
			iterator.remove();
			if (token.type() == TokenType.END) {
				break;
			} else if (token.type() == TokenType.INT) {
				assert token.data() instanceof Integer;
				stack.append((int) token.data());
			} else if (token.type() == TokenType.INTRINSIC) {
				if (stack.size() < 2) {
					throw new FeylonException(token.pos(), "Missing data for intrinsic '%s' when evaluating memory definition".formatted(token.data()));
				}
				final int a = stack.pop();
				final int b = stack.pop();
				switch ((Intrinsics) token.data()) {
					case ADD -> stack.append(a + b);
					case MULTIPLY -> stack.append(a * b);

					default -> throw new FeylonException(token.pos(), "Encountered illegal intrinsic '%s' when evaluating memory definition".formatted(token.data()));
				}
			} else if (token.type() == TokenType.CONST_REF) {
				final ConstDef constDef = ctx.constants.get(token.txt());
				if (constDef == null) {
					throw new FeylonException(token.pos(), "Encountered constant reference '%s' before it was defined when evaluating memory definition".formatted(token.txt()));
				}
				stack.append(constDef.value());
			} else {
				throw new FeylonException(token.pos(), "Encountered illegal '%s' token '%s' when evaluating memory definition".formatted(token.type(), token.txt()));
			}
		}
		if (stack.size() != 1) {
			throw new FeylonException(memoryToken.pos(), "The value of a memory definition should evaluate to a single number");
		}
		return stack.pop();
	}
}