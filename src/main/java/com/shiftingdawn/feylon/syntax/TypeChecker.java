package com.shiftingdawn.feylon.syntax;

import com.shiftingdawn.feylon.OrderedList;
import com.shiftingdawn.feylon.lang.Intrinsics;
import com.shiftingdawn.feylon.lang.Keywords;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TypeChecker {

	private static class Context {
		private OrderedList<PositionedType> stack;
		private int pointer;
		private final OrderedList<PositionedType> outputs;

		private Context(final OrderedList<PositionedType> stack, final int pointer, final OrderedList<PositionedType> outputs) {
			this.stack = stack;
			this.pointer = pointer;
			this.outputs = outputs;
		}
	}

	private record Signature(Collection<PositionedType> inputs, Collection<PositionedType> outputs) {
	}

	public static void check(final CompilerContext sources, final int shutdownStackSize) {
		final Map<Integer, OrderedList<PositionedType>> handledLoops = new HashMap<>();
		final Map<String, Signature> funcSigs = new HashMap<>();
		sources.functions.forEach((name, func) -> funcSigs.put(name, new Signature(new OrderedList<>(func.inputs()), new OrderedList<>(func.outputs()))));

		final OrderedList<Context> contexts = new OrderedList<>(List.of(new Context(new OrderedList<>(), 0, new OrderedList<>())));

		while (!contexts.isEmpty()) {
			final Context ctx = contexts.getLast();
			if (ctx.pointer >= sources.instructions.size()) {
				TypeChecker.checkOutputs(ctx, shutdownStackSize);
				contexts.removeLast();
				continue;
			}
			final InstructionSource instruction = sources.instructions.get(ctx.pointer);
			switch (instruction.type) {
				case PUSH_INT -> {
					ctx.stack.append(new PositionedType(DataType.INTEGER, instruction.pos));
					++ctx.pointer;
				}
				case PUSH_STRING -> {
					ctx.stack.append(new PositionedType(DataType.INTEGER, instruction.pos));
					ctx.stack.append(new PositionedType(DataType.POINTER, instruction.pos));
					++ctx.pointer;
				}
				case FUNCTION -> {
					ctx.pointer = (int) instruction.data;
				}
				case CALL -> {
					TypeChecker.checkSignature(instruction, ctx, funcSigs.get(instruction.txt));
					++ctx.pointer;
				}
				case RETURN -> {
					TypeChecker.checkOutputs(ctx, 0);
					contexts.removeLast();
				}
				case JUMP -> {
					assert instruction.data instanceof Integer;
					ctx.pointer = (int) instruction.data;
				}
				case IF -> {
					TypeChecker.checkSignature(instruction, ctx, new Signature(List.of(new PositionedType(DataType.BOOLEAN, instruction.pos)), List.of()));
					++ctx.pointer;
					if (!(instruction.data instanceof final Integer jumpPointer)) {
						throw new CompilerException(instruction.pos, CompilerErrors.UNCLOSED_STATEMENT, "Missing '%s' statement".formatted(Keywords.END.textValue));
					}
					contexts.append(new Context(new OrderedList<>(ctx.stack), jumpPointer, new OrderedList<>()));
				}
				case ELSE -> {
					assert instruction.data instanceof Integer;
					ctx.pointer = (int) instruction.data;
				}
				case WHILE -> {
					++ctx.pointer;
				}
				case DO -> {
					TypeChecker.checkSignature(instruction, ctx, new Signature(List.of(new PositionedType(DataType.BOOLEAN, instruction.pos)), List.of()));
					assert instruction.data instanceof Integer;
					if (handledLoops.containsKey(ctx.pointer)) {
						final OrderedList<DataType> expectedTypes = new OrderedList<>(handledLoops.get(ctx.pointer).stream().map(PositionedType::type).toList());
						final OrderedList<DataType> actualTypes = new OrderedList<>(ctx.stack.stream().map(PositionedType::type).toList());
						if (expectedTypes.size() != actualTypes.size() || !actualTypes.containsAll(expectedTypes) || !expectedTypes.containsAll(actualTypes)) {
							throw new CompilerException(instruction.pos, CompilerErrors.ILLEGAL_FRAME_MODIFICATION, "Loops are not allowed to modify the stack between iterations!")
									.add(instruction.pos, "Stack BEFORE loop:")
									.add(add -> {
										if (handledLoops.get(ctx.pointer).isEmpty()) {
											add.accept(instruction.pos, "<empty>");
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
						contexts.append(new Context(new OrderedList<>(ctx.stack), (int) instruction.data, new OrderedList<>(ctx.outputs)));
					}
				}
				case INTRINSIC -> {
					assert instruction.data instanceof Intrinsics;
					switch ((Intrinsics) instruction.data) {
						case TRUE, FALSE -> {
							ctx.stack.append(new PositionedType(DataType.BOOLEAN, instruction.pos));
						}
						case ADD -> TypeChecker.checkSignature(instruction, ctx, new Signature(
								List.of(new PositionedType(DataType.INTEGER, instruction.pos), new PositionedType(DataType.INTEGER, instruction.pos)),
								List.of(new PositionedType(DataType.INTEGER, instruction.pos))
						));
						case SUBTRACT -> TypeChecker.checkSignature(instruction, ctx, new Signature(
								List.of(new PositionedType(DataType.INTEGER, instruction.pos), new PositionedType(DataType.INTEGER, instruction.pos)),
								List.of(new PositionedType(DataType.INTEGER, instruction.pos))
						));
						case MULTIPLY -> TypeChecker.checkSignature(instruction, ctx, new Signature(
								List.of(new PositionedType(DataType.INTEGER, instruction.pos), new PositionedType(DataType.INTEGER, instruction.pos)),
								List.of(new PositionedType(DataType.INTEGER, instruction.pos))
						));
						case DIVIDE -> TypeChecker.checkSignature(instruction, ctx, new Signature(
								List.of(new PositionedType(DataType.INTEGER, instruction.pos), new PositionedType(DataType.INTEGER, instruction.pos)),
								List.of(new PositionedType(DataType.INTEGER, instruction.pos))
						));
						case MODULO -> TypeChecker.checkSignature(instruction, ctx, new Signature(
								List.of(new PositionedType(DataType.INTEGER, instruction.pos), new PositionedType(DataType.INTEGER, instruction.pos)),
								List.of(new PositionedType(DataType.INTEGER, instruction.pos))
						));
						case EQUALS -> TypeChecker.checkSignature(instruction, ctx, new Signature(
								List.of(new PositionedType(DataType.INTEGER, instruction.pos), new PositionedType(DataType.INTEGER, instruction.pos)),
								List.of(new PositionedType(DataType.BOOLEAN, instruction.pos))
						));
						case NOT_EQUALS -> TypeChecker.checkSignature(instruction, ctx, new Signature(
								List.of(new PositionedType(DataType.INTEGER, instruction.pos), new PositionedType(DataType.INTEGER, instruction.pos)),
								List.of(new PositionedType(DataType.BOOLEAN, instruction.pos))
						));
						case LESS -> TypeChecker.checkSignature(instruction, ctx, new Signature(
								List.of(new PositionedType(DataType.INTEGER, instruction.pos), new PositionedType(DataType.INTEGER, instruction.pos)),
								List.of(new PositionedType(DataType.BOOLEAN, instruction.pos))
						));
						case GREATER -> TypeChecker.checkSignature(instruction, ctx, new Signature(
								List.of(new PositionedType(DataType.INTEGER, instruction.pos), new PositionedType(DataType.INTEGER, instruction.pos)),
								List.of(new PositionedType(DataType.BOOLEAN, instruction.pos))
						));
						case LESS_OR_EQUAL -> TypeChecker.checkSignature(instruction, ctx, new Signature(
								List.of(new PositionedType(DataType.INTEGER, instruction.pos), new PositionedType(DataType.INTEGER, instruction.pos)),
								List.of(new PositionedType(DataType.BOOLEAN, instruction.pos))
						));
						case GREATER_OR_EQUAL -> TypeChecker.checkSignature(instruction, ctx, new Signature(
								List.of(new PositionedType(DataType.INTEGER, instruction.pos), new PositionedType(DataType.INTEGER, instruction.pos)),
								List.of(new PositionedType(DataType.BOOLEAN, instruction.pos))
						));
						case SHIFT_LEFT -> TypeChecker.checkSignature(instruction, ctx, new Signature(
								List.of(new PositionedType(DataType.INTEGER, instruction.pos), new PositionedType(DataType.INTEGER, instruction.pos)),
								List.of(new PositionedType(DataType.INTEGER, instruction.pos))
						));
						case SHIFT_RIGHT -> TypeChecker.checkSignature(instruction, ctx, new Signature(
								List.of(new PositionedType(DataType.INTEGER, instruction.pos), new PositionedType(DataType.INTEGER, instruction.pos)),
								List.of(new PositionedType(DataType.INTEGER, instruction.pos))
						));
						case BITWISE_AND -> TypeChecker.checkSignature(instruction, ctx, new Signature(
								List.of(new PositionedType(DataType.INTEGER, instruction.pos), new PositionedType(DataType.INTEGER, instruction.pos)),
								List.of(new PositionedType(DataType.INTEGER, instruction.pos))
						));
						case BITWISE_OR -> TypeChecker.checkSignature(instruction, ctx, new Signature(
								List.of(new PositionedType(DataType.INTEGER, instruction.pos), new PositionedType(DataType.INTEGER, instruction.pos)),
								List.of(new PositionedType(DataType.INTEGER, instruction.pos))
						));
						case BITWISE_XOR -> TypeChecker.checkSignature(instruction, ctx, new Signature(
								List.of(new PositionedType(DataType.INTEGER, instruction.pos), new PositionedType(DataType.INTEGER, instruction.pos)),
								List.of(new PositionedType(DataType.INTEGER, instruction.pos))
						));
						case CAST_INTEGER -> {
							final var a = TypeChecker.checkArity(ctx, instruction, 1)[0];
							TypeChecker.checkSignature(instruction, ctx, new Signature(List.of(a), List.of(new PositionedType(DataType.INTEGER, a.pos()))));
						}
						case CAST_BOOLEAN -> {
							final var a = TypeChecker.checkArity(ctx, instruction, 1)[0];
							TypeChecker.checkSignature(instruction, ctx, new Signature(List.of(a), List.of(new PositionedType(DataType.BOOLEAN, a.pos()))));
						}
						case CAST_POINTER -> {
							final var a = TypeChecker.checkArity(ctx, instruction, 1)[0];
							TypeChecker.checkSignature(instruction, ctx, new Signature(List.of(a), List.of(new PositionedType(DataType.POINTER, a.pos()))));
						}
						default -> throw new AssertionError("Encountered unknown intrinsic: " + instruction.data);
					}
					++ctx.pointer;
				}
				case INSTRUCTION -> {
					switch (Operations.getByText(instruction.txt).orElseThrow(() -> new AssertionError("Unknown operation: " + instruction.txt))) {
						case NOOP -> {
						}
						case POP -> {
							final var a = TypeChecker.checkArity(ctx, instruction, 1)[0];
							TypeChecker.checkSignature(instruction, ctx, new Signature(List.of(a), List.of()));
						}
						case DUP -> {
							final var a = TypeChecker.checkArity(ctx, instruction, 1)[0];
							TypeChecker.checkSignature(instruction, ctx, new Signature(List.of(a), List.of(a, a)));
						}
						case SWAP -> {
							final var ab = TypeChecker.checkArity(ctx, instruction, 2);
							TypeChecker.checkSignature(instruction, ctx, new Signature(List.of(ab[0], ab[1]), List.of(ab[1], ab[0])));
						}
						case OVER -> {
							final var ab = TypeChecker.checkArity(ctx, instruction, 2);
							TypeChecker.checkSignature(instruction, ctx, new Signature(List.of(ab[0], ab[1]), List.of(ab[0], ab[1], ab[0])));
						}
						case ROT -> {
							final var abc = TypeChecker.checkArity(ctx, instruction, 3);
							TypeChecker.checkSignature(instruction, ctx, new Signature(List.of(abc[0], abc[1], abc[2]), List.of(abc[1], abc[2], abc[0])));
						}
						case SYSCALL3 -> {
							final var abcn = TypeChecker.checkArity(ctx, instruction, 4);
							TypeChecker.checkSignature(instruction, ctx, new Signature(List.of(abcn), List.of()));
						}
						case MEM -> {
						}
						case MEMGET -> TypeChecker.checkSignature(instruction, ctx, new Signature(
								List.of(new PositionedType(DataType.POINTER, instruction.pos)),
								List.of(new PositionedType(DataType.INTEGER, instruction.pos)))
						);
						case MEMSET -> {
							final var dp = TypeChecker.checkArity(ctx, instruction, 2);
							TypeChecker.checkSignature(instruction, ctx, new Signature(List.of(new PositionedType(DataType.POINTER, dp[1].pos()), dp[0]), List.of()));
						}
						default -> throw new AssertionError("Encountered unknown operation: " + instruction.data);
					}
					++ctx.pointer;
				}
			}
		}
	}

	private static PositionedType[] checkArity(final Context ctx, final InstructionSource src, final int count) {
		final PositionedType[] result = new PositionedType[count];
		if (ctx.stack.size() < count) {
			throw new CompilerException(src.pos, CompilerErrors.MISSING_ARGUMENTS, "Not enough arguments were provided for '%s'. Expected %s but got %s".formatted(src.txt, count, ctx.stack.size()));
		}
		for (int i = 0; i < count; ++i) {
			result[i] = ctx.stack.get(ctx.stack.size() - 1 - i);
		}
		return result;
	}

	private static void checkSignature(final InstructionSource src, final Context ctx, final Signature signature) {
		final OrderedList<PositionedType> inputs = new OrderedList<>(signature.inputs());
		final OrderedList<PositionedType> stack = new OrderedList<>(ctx.stack);
		int argCount = 0;
		while (!stack.isEmpty() && !inputs.isEmpty()) {
			final PositionedType expected = inputs.pop();
			final PositionedType actual = stack.pop();
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
							final PositionedType item = inputs.pop();
							add.accept(item.pos(), item.type().toString());
						}
					});
		}
		signature.outputs().forEach(stack::append);
		ctx.stack = stack;
	}

	private static void checkOutputs(final Context ctx, final int allowedOverflow) {
		while (!ctx.stack.isEmpty() && !ctx.outputs.isEmpty()) {
			final PositionedType expected = ctx.outputs.pop();
			final PositionedType actual = ctx.stack.pop();
			if (expected.type() != actual.type()) {
				throw new CompilerException(actual.pos(), CompilerErrors.INVALID_DATA, "Unexpected type '%s' placed on the stack.".formatted(actual.type()))
						.add(expected.pos(), "Expected type: '%s'".formatted(expected.type()));
			}
		}
		if (ctx.stack.size() - allowedOverflow > ctx.outputs.size()) {
			throw new CompilerException(ctx.stack.getLast().pos(), CompilerErrors.UNHANDLED_DATA, "Found unhandled data on the stack:")
					.add(add -> {
						while (!ctx.stack.isEmpty()) {
							final PositionedType item = ctx.stack.pop();
							add.accept(item.pos(), "type '%s'".formatted(item.type()));
						}
					});
		} else if (ctx.stack.size() < ctx.outputs.size()) {
			throw new CompilerException(ctx.outputs.getLast().pos(), CompilerErrors.MISSING_DATA, "Missing data on the stack. Expected:")
					.add(add -> {
						while (!ctx.outputs.isEmpty()) {
							final PositionedType item = ctx.stack.pop();
							add.accept(item.pos(), "type '%s'".formatted(item.type()));
						}
					});
		}
	}
}