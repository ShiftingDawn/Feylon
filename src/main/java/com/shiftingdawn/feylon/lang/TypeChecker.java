package com.shiftingdawn.feylon.lang;

import com.shiftingdawn.feylon.OrderedList;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class TypeChecker {

	private static class Context {
		private OrderedList<TypedPos> stack;
		private int pointer;
		private final OrderedList<TypedPos> outputs;

		private Context(final OrderedList<TypedPos> stack, final int pointer, final OrderedList<TypedPos> outputs) {
			this.stack = stack;
			this.pointer = pointer;
			this.outputs = outputs;
		}
	}

	private record Signature(Collection<TypedPos> inputs, Collection<TypedPos> outputs) {
	}

	public static void check(final LinkerContext linkerContext, final int shutdownStackSize) {
		final Map<Integer, OrderedList<TypedPos>> handledLoops = new HashMap<>();
		final Map<String, Signature> funcSigs = new HashMap<>();
		linkerContext.functions.forEach((name, func) -> funcSigs.put(name, new Signature(new OrderedList<>(func.inputs), new OrderedList<>(func.outputs))));

		final OrderedList<Context> contexts = new OrderedList<>(List.of(new Context(new OrderedList<>(), 0, new OrderedList<>())));

		while (!contexts.isEmpty()) {
			final Context ctx = contexts.getLast();
			if (ctx.pointer >= linkerContext.result.size()) {
				TypeChecker.checkOutputs(ctx, shutdownStackSize);
				contexts.removeLast();
				continue;
			}
			final LinkedToken linkedToken = linkerContext.result.get(ctx.pointer);
			switch (linkedToken.type) {
				case PUSH_INT -> {
					ctx.stack.append(new TypedPos(linkedToken.pos, DataType.INT));
					++ctx.pointer;
				}
				case PUSH_BOOL -> {
					ctx.stack.append(new TypedPos(linkedToken.pos, DataType.BOOL));
					++ctx.pointer;
				}
				case PUSH_STRING -> {
					ctx.stack.append(new TypedPos(linkedToken.pos, DataType.INT));
					ctx.stack.append(new TypedPos(linkedToken.pos, DataType.POINTER));
					++ctx.pointer;
				}
				case FUNCTION -> {
					ctx.pointer = (int) linkedToken.data;
				}
				case CALL -> {
					TypeChecker.checkSignature(linkedToken, ctx, funcSigs.get(linkedToken.txt));
					++ctx.pointer;
				}
				case RETURN -> {
					TypeChecker.checkOutputs(ctx, 0);
					contexts.removeLast();
				}
				case JUMP -> {
					assert linkedToken.data instanceof Integer;
					ctx.pointer = (int) linkedToken.data;
				}
				case JUMP_EQ, JUMP_NEQ -> {
					TypeChecker.checkSignature(linkedToken, ctx, new Signature(List.of(new TypedPos(linkedToken.pos, DataType.BOOL)), List.of()));
					++ctx.pointer;
					if (!(linkedToken.data instanceof final Integer jumpPointer)) {
						throw new CompilerException(linkedToken.pos, CompilerErrors.UNCLOSED_STATEMENT, "Missing '%s' statement".formatted(Keywords.END.textValue));
					}
					contexts.append(new Context(new OrderedList<>(ctx.stack), jumpPointer, new OrderedList<>()));
				}
				case DO -> {
					TypeChecker.checkSignature(linkedToken, ctx, new Signature(List.of(new TypedPos(linkedToken.pos, DataType.BOOL)), List.of()));
					assert linkedToken.data instanceof Integer;
					if (handledLoops.containsKey(ctx.pointer)) {
						final OrderedList<DataType> expectedTypes = new OrderedList<>(handledLoops.get(ctx.pointer).stream().map(TypedPos::type).toList());
						final OrderedList<DataType> actualTypes = new OrderedList<>(ctx.stack.stream().map(TypedPos::type).toList());
						if (expectedTypes.size() != actualTypes.size() || !actualTypes.containsAll(expectedTypes) || !expectedTypes.containsAll(actualTypes)) {
							throw new CompilerException(linkedToken.pos, CompilerErrors.ILLEGAL_FRAME_MODIFICATION, "Loops are not allowed to modify the stack between iterations!")
									.add(linkedToken.pos, "Stack BEFORE loop:")
									.add(add -> {
										if (handledLoops.get(ctx.pointer).isEmpty()) {
											add.accept(linkedToken.pos, "<empty>");
										} else {
											handledLoops.get(ctx.pointer).forEach(elem -> {
												add.accept(elem.pos(), elem.type().toString());
											});
										}
									});
						}
						contexts.pop();
					} else {
						handledLoops.put(ctx.pointer, new OrderedList<>(ctx.stack));
						++ctx.pointer;
						contexts.append(new Context(new OrderedList<>(ctx.stack), (int) linkedToken.data, new OrderedList<>(ctx.outputs)));
					}
				}
				case INTRINSIC -> {
					assert linkedToken.data instanceof Intrinsics;
					switch ((Intrinsics) linkedToken.data) {
						case TRUE, FALSE -> {
							ctx.stack.append(new TypedPos(linkedToken.pos, DataType.BOOL));
						}
						case ADD -> TypeChecker.checkSignature(linkedToken, ctx, new Signature(
								List.of(new TypedPos(linkedToken.pos, DataType.INT), new TypedPos(linkedToken.pos, DataType.INT)),
								List.of(new TypedPos(linkedToken.pos, DataType.INT))
						));
						case SUBTRACT -> TypeChecker.checkSignature(linkedToken, ctx, new Signature(
								List.of(new TypedPos(linkedToken.pos, DataType.INT), new TypedPos(linkedToken.pos, DataType.INT)),
								List.of(new TypedPos(linkedToken.pos, DataType.INT))
						));
						case MULTIPLY -> TypeChecker.checkSignature(linkedToken, ctx, new Signature(
								List.of(new TypedPos(linkedToken.pos, DataType.INT), new TypedPos(linkedToken.pos, DataType.INT)),
								List.of(new TypedPos(linkedToken.pos, DataType.INT))
						));
						case DIVIDE -> TypeChecker.checkSignature(linkedToken, ctx, new Signature(
								List.of(new TypedPos(linkedToken.pos, DataType.INT), new TypedPos(linkedToken.pos, DataType.INT)),
								List.of(new TypedPos(linkedToken.pos, DataType.INT))
						));
						case MODULO -> TypeChecker.checkSignature(linkedToken, ctx, new Signature(
								List.of(new TypedPos(linkedToken.pos, DataType.INT), new TypedPos(linkedToken.pos, DataType.INT)),
								List.of(new TypedPos(linkedToken.pos, DataType.INT))
						));
						case EQUALS -> TypeChecker.checkSignature(linkedToken, ctx, new Signature(
								List.of(new TypedPos(linkedToken.pos, DataType.INT), new TypedPos(linkedToken.pos, DataType.INT)),
								List.of(new TypedPos(linkedToken.pos, DataType.BOOL))
						));
						case NOT_EQUALS -> TypeChecker.checkSignature(linkedToken, ctx, new Signature(
								List.of(new TypedPos(linkedToken.pos, DataType.INT), new TypedPos(linkedToken.pos, DataType.INT)),
								List.of(new TypedPos(linkedToken.pos, DataType.BOOL))
						));
						case LESS -> TypeChecker.checkSignature(linkedToken, ctx, new Signature(
								List.of(new TypedPos(linkedToken.pos, DataType.INT), new TypedPos(linkedToken.pos, DataType.INT)),
								List.of(new TypedPos(linkedToken.pos, DataType.BOOL))
						));
						case GREATER -> TypeChecker.checkSignature(linkedToken, ctx, new Signature(
								List.of(new TypedPos(linkedToken.pos, DataType.INT), new TypedPos(linkedToken.pos, DataType.INT)),
								List.of(new TypedPos(linkedToken.pos, DataType.BOOL))
						));
						case LESS_OR_EQUAL -> TypeChecker.checkSignature(linkedToken, ctx, new Signature(
								List.of(new TypedPos(linkedToken.pos, DataType.INT), new TypedPos(linkedToken.pos, DataType.INT)),
								List.of(new TypedPos(linkedToken.pos, DataType.BOOL))
						));
						case GREATER_OR_EQUAL -> TypeChecker.checkSignature(linkedToken, ctx, new Signature(
								List.of(new TypedPos(linkedToken.pos, DataType.INT), new TypedPos(linkedToken.pos, DataType.INT)),
								List.of(new TypedPos(linkedToken.pos, DataType.BOOL))
						));
						case SHIFT_LEFT -> TypeChecker.checkSignature(linkedToken, ctx, new Signature(
								List.of(new TypedPos(linkedToken.pos, DataType.INT), new TypedPos(linkedToken.pos, DataType.INT)),
								List.of(new TypedPos(linkedToken.pos, DataType.INT))
						));
						case SHIFT_RIGHT -> TypeChecker.checkSignature(linkedToken, ctx, new Signature(
								List.of(new TypedPos(linkedToken.pos, DataType.INT), new TypedPos(linkedToken.pos, DataType.INT)),
								List.of(new TypedPos(linkedToken.pos, DataType.INT))
						));
						case BITWISE_AND -> TypeChecker.checkSignature(linkedToken, ctx, new Signature(
								List.of(new TypedPos(linkedToken.pos, DataType.INT), new TypedPos(linkedToken.pos, DataType.INT)),
								List.of(new TypedPos(linkedToken.pos, DataType.INT))
						));
						case BITWISE_OR -> TypeChecker.checkSignature(linkedToken, ctx, new Signature(
								List.of(new TypedPos(linkedToken.pos, DataType.INT), new TypedPos(linkedToken.pos, DataType.INT)),
								List.of(new TypedPos(linkedToken.pos, DataType.INT))
						));
						case BITWISE_XOR -> TypeChecker.checkSignature(linkedToken, ctx, new Signature(
								List.of(new TypedPos(linkedToken.pos, DataType.INT), new TypedPos(linkedToken.pos, DataType.INT)),
								List.of(new TypedPos(linkedToken.pos, DataType.INT))
						));
						case CAST_INTEGER -> {
							final var a = TypeChecker.checkArity(ctx, linkedToken, 1)[0];
							TypeChecker.checkSignature(linkedToken, ctx, new Signature(List.of(a), List.of(new TypedPos(a.pos(), DataType.INT))));
						}
						case CAST_BOOLEAN -> {
							final var a = TypeChecker.checkArity(ctx, linkedToken, 1)[0];
							TypeChecker.checkSignature(linkedToken, ctx, new Signature(List.of(a), List.of(new TypedPos(a.pos(), DataType.BOOL))));
						}
						case CAST_POINTER -> {
							final var a = TypeChecker.checkArity(ctx, linkedToken, 1)[0];
							TypeChecker.checkSignature(linkedToken, ctx, new Signature(List.of(a), List.of(new TypedPos(a.pos(), DataType.POINTER))));
						}
						case DUMP -> {
							final var a = TypeChecker.checkArity(ctx, linkedToken, 1)[0];
							TypeChecker.checkSignature(linkedToken, ctx, new Signature(List.of(a), List.of()));
						}
						case POP -> {
							final var a = TypeChecker.checkArity(ctx, linkedToken, 1)[0];
							TypeChecker.checkSignature(linkedToken, ctx, new Signature(List.of(a), List.of()));
						}
						case DUP -> {
							final var a = TypeChecker.checkArity(ctx, linkedToken, 1)[0];
							TypeChecker.checkSignature(linkedToken, ctx, new Signature(List.of(a), List.of(a, a)));
						}
						case SWAP -> {
							final var ab = TypeChecker.checkArity(ctx, linkedToken, 2);
							TypeChecker.checkSignature(linkedToken, ctx, new Signature(List.of(ab[0], ab[1]), List.of(ab[1], ab[0])));
						}
						case OVER -> {
							final var ab = TypeChecker.checkArity(ctx, linkedToken, 2);
							TypeChecker.checkSignature(linkedToken, ctx, new Signature(List.of(ab[0], ab[1]), List.of(ab[0], ab[1], ab[0])));
						}
						case ROT -> {
							final var abc = TypeChecker.checkArity(ctx, linkedToken, 3);
							TypeChecker.checkSignature(linkedToken, ctx, new Signature(List.of(abc[0], abc[1], abc[2]), List.of(abc[1], abc[2], abc[0])));
						}
						default -> throw new AssertionError("Encountered unknown intrinsic: " + linkedToken.data);
					}
					++ctx.pointer;
				}
//				case INSTRUCTION -> {
//					switch (Operations.getByText(linkedToken.txt).orElseThrow(() -> new AssertionError("Unknown operation: " + linkedToken.txt))) {
//						case NOOP -> {
//						}
//						case SYSCALL3 -> {
//							final var abcn = TypeChecker.checkArity(ctx, linkedToken, 4);
//							TypeChecker.checkSignature(linkedToken, ctx, new Signature(List.of(abcn), List.of()));
//						}
//						case MEM -> {
//						}
//						case MEMGET -> TypeChecker.checkSignature(linkedToken, ctx, new Signature(
//								List.of(new TypedPos(DataType.POINTER, linkedToken.pos)),
//								List.of(new TypedPos(linkedToken.pos, DataType.INT)))
//						);
//						case MEMSET -> {
//							final var dp = TypeChecker.checkArity(ctx, linkedToken, 2);
//							TypeChecker.checkSignature(linkedToken, ctx, new Signature(List.of(new TypedPos(DataType.POINTER, dp[1].pos()), dp[0]), List.of()));
//						}
//
//						default -> throw new AssertionError("Encountered unknown operation: " + linkedToken.data);
//					}
//					++ctx.pointer;
//				}
				default -> throw new AssertionError("Encountered unhandled token: " + linkedToken.type);
			}
		}
	}

	private static TypedPos[] checkArity(final Context ctx, final LinkedToken src, final int count) {
		final TypedPos[] result = new TypedPos[count];
		if (ctx.stack.size() < count) {
			throw new CompilerException(src.pos, CompilerErrors.MISSING_ARGUMENTS, "Not enough arguments were provided for '%s'. Expected %s but got %s".formatted(src.txt, count, ctx.stack.size()));
		}
		for (int i = 0; i < count; ++i) {
			result[i] = ctx.stack.get(ctx.stack.size() - 1 - i);
		}
		return result;
	}

	private static void checkSignature(final LinkedToken src, final Context ctx, final Signature signature) {
		final OrderedList<TypedPos> inputs = new OrderedList<>(signature.inputs());
		final OrderedList<TypedPos> stack = new OrderedList<>(ctx.stack);
		int argCount = 0;
		while (!stack.isEmpty() && !inputs.isEmpty()) {
			final TypedPos expected = inputs.pop();
			final TypedPos actual = stack.pop();
			if (expected.type() != actual.type()) {
				throw new CompilerException(src.pos, CompilerErrors.INVALID_DATA, "Argument %s of %s is expected to be type '%s' but received type '%s' instead.".formatted(argCount, src.txt, expected.type(), actual.type()))
						.add(actual.pos(), "Argument %s was found here".formatted(argCount))
						.add(expected.pos(), "Expected type is defined here");
			}
			++argCount;
		}
		if (stack.size() < inputs.size()) {
			throw new CompilerException(src.pos, CompilerErrors.MISSING_ARGUMENTS, "Not enough arguments were provided for '%s' '%s'.".formatted(src.type, src.txt))
					.add(src.pos, "Missing arguments:")
					.add(add -> {
						while (!inputs.isEmpty()) {
							final TypedPos item = inputs.pop();
							add.accept(item.pos(), item.type().toString());
						}
					});
		}
		signature.outputs().forEach(stack::append);
		ctx.stack = stack;
	}

	private static void checkOutputs(final Context ctx, final int allowedOverflow) {
		while (!ctx.stack.isEmpty() && !ctx.outputs.isEmpty()) {
			final TypedPos expected = ctx.outputs.pop();
			final TypedPos actual = ctx.stack.pop();
			if (expected.type() != actual.type()) {
				throw new CompilerException(actual.pos(), CompilerErrors.INVALID_DATA, "Unexpected type '%s' placed on the stack.".formatted(actual.type()))
						.add(expected.pos(), "Expected type: '%s'".formatted(expected.type()));
			}
		}
		if (ctx.stack.size() - allowedOverflow > ctx.outputs.size()) {
			throw new CompilerException(ctx.stack.getLast().pos(), CompilerErrors.UNHANDLED_DATA, "Found unhandled data on the stack:")
					.add(add -> {
						while (!ctx.stack.isEmpty()) {
							final TypedPos item = ctx.stack.pop();
							add.accept(item.pos(), "type '%s'".formatted(item.type()));
						}
					});
		} else if (ctx.stack.size() < ctx.outputs.size()) {
			throw new CompilerException(ctx.outputs.getLast().pos(), CompilerErrors.MISSING_DATA, "Missing data on the stack. Expected:")
					.add(add -> {
						while (!ctx.outputs.isEmpty()) {
							final TypedPos item = ctx.stack.pop();
							add.accept(item.pos(), "type '%s'".formatted(item.type()));
						}
					});
		}
	}
}