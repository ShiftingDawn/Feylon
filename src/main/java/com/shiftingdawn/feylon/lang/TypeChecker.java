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
				case PUSH_POINTER -> {
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
						throw new FeylonException(linkedToken.pos, "Missing '%s' statement".formatted(Keywords.END.textValue));
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
							throw new FeylonException(linkedToken.pos, "Loops are not allowed to modify the stack between iterations!")
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
						case ADD -> TypeChecker.checkSignature(linkedToken, ctx,
								new Signature(
										List.of(new TypedPos(linkedToken.pos, DataType.INT), new TypedPos(linkedToken.pos, DataType.INT)),
										List.of(new TypedPos(linkedToken.pos, DataType.INT))
								),
								new Signature(
										List.of(new TypedPos(linkedToken.pos, DataType.POINTER), new TypedPos(linkedToken.pos, DataType.INT)),
										List.of(new TypedPos(linkedToken.pos, DataType.POINTER))
								),
								new Signature(
										List.of(new TypedPos(linkedToken.pos, DataType.INT), new TypedPos(linkedToken.pos, DataType.POINTER)),
										List.of(new TypedPos(linkedToken.pos, DataType.POINTER))
								)
						);
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
						case STORE, STORE_16, STORE_32 -> TypeChecker.checkSignature(linkedToken, ctx, new Signature(
								List.of(new TypedPos(linkedToken.pos, DataType.INT), new TypedPos(linkedToken.pos, DataType.POINTER)),
								List.of()
						));
						case LOAD, LOAD_16, LOAD_32 -> TypeChecker.checkSignature(linkedToken, ctx, new Signature(
								List.of(new TypedPos(linkedToken.pos, DataType.POINTER)),
								List.of(new TypedPos(linkedToken.pos, DataType.INT))
						));
						case SYSCALL_0 -> {
							final var n = TypeChecker.checkArity(ctx, linkedToken, 1);
							TypeChecker.checkSignature(linkedToken, ctx, new Signature(List.of(n), List.of()));
						}
						case SYSCALL_1 -> {
							final var an = TypeChecker.checkArity(ctx, linkedToken, 2);
							TypeChecker.checkSignature(linkedToken, ctx, new Signature(List.of(an), List.of()));
						}
						case SYSCALL_2 -> {
							final var abn = TypeChecker.checkArity(ctx, linkedToken, 3);
							TypeChecker.checkSignature(linkedToken, ctx, new Signature(List.of(abn), List.of()));
						}
						case SYSCALL_3 -> {
							final var abcn = TypeChecker.checkArity(ctx, linkedToken, 4);
							TypeChecker.checkSignature(linkedToken, ctx, new Signature(List.of(abcn), List.of()));
						}
						case SYSCALL_4 -> {
							final var abcdn = TypeChecker.checkArity(ctx, linkedToken, 5);
							TypeChecker.checkSignature(linkedToken, ctx, new Signature(List.of(abcdn), List.of()));
						}
						case SYSCALL_5 -> {
							final var abcden = TypeChecker.checkArity(ctx, linkedToken, 6);
							TypeChecker.checkSignature(linkedToken, ctx, new Signature(List.of(abcden), List.of()));
						}
						case SYSCALL_6 -> {
							final var abcdefn = TypeChecker.checkArity(ctx, linkedToken, 7);
							TypeChecker.checkSignature(linkedToken, ctx, new Signature(List.of(abcdefn), List.of()));
						}
						default -> throw new AssertionError("Encountered unknown intrinsic: " + linkedToken.data);
					}
					++ctx.pointer;
				}
				default -> throw new AssertionError("Encountered unhandled token: " + linkedToken.type);
			}
		}
	}

	private static TypedPos[] checkArity(final Context ctx, final LinkedToken src, final int count) {
		final TypedPos[] result = new TypedPos[count];
		if (ctx.stack.size() < count) {
			throw new FeylonException(src.pos, "Not enough arguments were provided for '%s'. Expected %s but got %s".formatted(src.txt, count, ctx.stack.size()));
		}
		for (int i = 0; i < count; ++i) {
			result[i] = ctx.stack.get(ctx.stack.size() - 1 - i);
		}
		return result;
	}

	private static void checkSignature(final LinkedToken src, final Context ctx, final Signature... signatures) {
		FeylonException ex = null;
		MainLoop:
		for (final Signature signature : signatures) {
			final OrderedList<TypedPos> inputs = new OrderedList<>(signature.inputs());
			final OrderedList<TypedPos> stack = new OrderedList<>(ctx.stack);
			int argCount = 0;
			while (!stack.isEmpty() && !inputs.isEmpty()) {
				final TypedPos expected = inputs.pop();
				final TypedPos actual = stack.pop();
				if (expected.type() != actual.type()) {
					final String errorMsg = "Argument %s of %s is expected to be type '%s' but received type '%s' instead.".formatted(argCount, src.txt, expected.type(), actual.type());
					if (ex == null) {
						ex = new FeylonException(src.pos, errorMsg);
					} else {
						ex.error(src.pos, errorMsg);
					}
					ex.add(actual.pos(), "Argument %s was found here".formatted(argCount))
							.add(expected.pos(), "Expected type is defined here");
					continue MainLoop;
				}
				++argCount;
			}
			if (stack.size() < inputs.size()) {
				if (ex == null) {
					ex = new FeylonException(src.pos, "Not enough arguments were provided for '%s' '%s'.".formatted(src.type, src.txt));
				} else {
					ex.error(src.pos, "Not enough arguments were provided for '%s' '%s'.".formatted(src.type, src.txt));
				}
				ex.add(src.pos, "Missing arguments:").add((adder) -> {
					while (!inputs.isEmpty()) {
						final TypedPos item = inputs.pop();
						adder.accept(item.pos(), item.type().toString());
					}
				});
				continue;
			}
			signature.outputs().forEach(stack::append);
			ctx.stack = stack;
			return;
		}
		assert ex != null;
		throw ex;
	}

	private static void checkOutputs(final Context ctx, final int allowedOverflow) {
		while (!ctx.stack.isEmpty() && !ctx.outputs.isEmpty()) {
			final TypedPos expected = ctx.outputs.pop();
			final TypedPos actual = ctx.stack.pop();
			if (expected.type() != actual.type()) {
				throw new FeylonException(actual.pos(), "Unexpected type '%s' placed on the stack.".formatted(actual.type()))
						.add(expected.pos(), "Expected type: '%s'".formatted(expected.type()));
			}
		}
		if (ctx.stack.size() - allowedOverflow > ctx.outputs.size()) {
			throw new FeylonException(ctx.stack.getLast().pos(), "Found unhandled data on the stack:")
					.add(add -> {
						while (!ctx.stack.isEmpty()) {
							final TypedPos item = ctx.stack.pop();
							add.accept(item.pos(), "type '%s'".formatted(item.type()));
						}
					});
		} else if (ctx.stack.size() < ctx.outputs.size()) {
			throw new FeylonException(ctx.outputs.getLast().pos(), "Missing data on the stack. Expected:")
					.add(add -> {
						while (!ctx.outputs.isEmpty()) {
							final TypedPos item = ctx.stack.pop();
							add.accept(item.pos(), "type '%s'".formatted(item.type()));
						}
					});
		}
	}
}