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
					throw new FeylonException(token.pos(), "Encountered incomplete cponstant");
				}
				final ConstValue value = Evaluator.evaluateConstant(token, ctx, iterator);
				ctx.constants.put(token.txt(), new ConstDef(token.pos(), value.type(), value.value()));
			}
		}
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
				final ConstValue a = stack.pop();
				final ConstValue b = stack.pop();
				if (a.type() != DataType.INT) {
					throw new CompilerException(token.pos(), CompilerErrors.INVALID_DATA, "Encountered invalid data type '%s' when evaluating constant".formatted(a.value()));
				} else if (b.type() != DataType.INT) {
					throw new CompilerException(token.pos(), CompilerErrors.INVALID_DATA, "Encountered invalid data type '%s' when evaluating constant".formatted(b.value()));
				}
				switch ((Intrinsics) token.data()) {
					case ADD -> stack.append(new ConstValue(DataType.INT, a.value() + b.value()));
					case SUBTRACT -> stack.append(new ConstValue(DataType.INT, b.value() - a.value()));
					case MULTIPLY -> stack.append(new ConstValue(DataType.INT, a.value() * b.value()));

					default -> throw new CompilerException(token.pos(), CompilerErrors.ILLEGAL_CONSTANT_VALUE,
							"Encountered illegal intrinsic '%s' when evaluating constant".formatted(token.data()));
				}
			} else if (token.type() == TokenType.CONST_REF) {
				final ConstDef constDef = ctx.constants.get(token.txt());
				if (constDef == null) {
					throw new CompilerException(token.pos(), CompilerErrors.ILLEGAL_CONSTANT_VALUE,
							"Encountered constant reference '%s' before it was defined when evaluating constant".formatted(token.txt()));
				}
				stack.append(new ConstValue(constDef.dataType(), constDef.value()));
			} else {
				throw new CompilerException(token.pos(), CompilerErrors.ILLEGAL_CONSTANT_VALUE,
						"Encountered illegal '%s' token '%s' when evaluating constant".formatted(token.type(), token.txt()));
			}
		}
		if (stack.size() != 1) {
			throw new CompilerException(constToken.pos(), CompilerErrors.ILLEGAL_CONSTANT_VALUE, "The value of a constant should evaluate to a single number");
		}
		return stack.pop();
	}
}