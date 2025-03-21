package com.shiftingdawn.feylon.lang;

import com.shiftingdawn.feylon.Instruction;
import com.shiftingdawn.feylon.OrderedList;
import com.shiftingdawn.feylon.ins.*;
import com.shiftingdawn.feylon.ins.jump.*;

final class Assembler {

	public static AssembledProgram assemble(final LinkerContext linkerContext) {
		final OrderedList<LinkedToken> tokens = new OrderedList<>(linkerContext.result).reverse();
		final AssemblerContext ctx = new AssemblerContext();
		final int skippedPointerOffset = 0;
		while (!tokens.isEmpty()) {
			final LinkedToken token = tokens.pop();
			switch (token.type) {
				case PUSH_INT -> ctx.result.append(new PushIntInstruction((int) token.data));
				case PUSH_BOOL -> ctx.result.append(new PushBooleanInstruction((boolean) token.data));
				case PUSH_STRING -> ctx.result.append(new PushStringInstruction((String) token.data));
				case INTRINSIC -> Assembler.processIntrinsic(ctx, token);

				case FUNCTION -> ctx.result.append(new JumpInstruction((int) token.data - skippedPointerOffset));
				case CALL -> ctx.result.append(new CallFunctionInstruction(linkerContext.functions.get(token.txt).pointer - skippedPointerOffset + 1, (token.selfPointer - skippedPointerOffset) + 1));
				case RETURN -> ctx.result.append(new ReturnInstruction());

				case JUMP -> ctx.result.append(new JumpInstruction((int) token.data - skippedPointerOffset));
				case JUMP_EQ -> ctx.result.append(new JumpIfInstruction((int) token.data - skippedPointerOffset));
				case JUMP_NEQ -> ctx.result.append(new JumpIfNotInstruction((int) token.data - skippedPointerOffset));
				case DO -> ctx.result.append(new DoInstruction((int) token.data - skippedPointerOffset));

				default -> throw new AssertionError("Encountered unhandled token: " + token.type);
			}
		}
		return new AssembledProgram(ctx.result.toArray(Instruction[]::new));
	}

	private static void processIntrinsic(final AssemblerContext ctx, final LinkedToken token) {
		ctx.result.append(switch ((Intrinsics) token.data) {
			case TRUE -> new PushBooleanInstruction(true);
			case FALSE -> new PushBooleanInstruction(false);
			case ADD -> new AddInstruction();
			case SUBTRACT -> new SubtractInstruction();
			case MULTIPLY -> new MultiplyInstruction();
			case DIVIDE -> new DivideInstruction();
			case MODULO -> new ModInstruction();
			case EQUALS -> new EqualsInstruction();
			case NOT_EQUALS -> new NotEqualsInstruction();
			case LESS -> new LessInstruction();
			case GREATER -> new GreaterInstruction();
			case LESS_OR_EQUAL -> new LessEqualInstruction();
			case GREATER_OR_EQUAL -> new GreaterEqualInstruction();
			case SHIFT_LEFT -> new BitShiftLeftInstruction();
			case SHIFT_RIGHT -> new BitShiftRightInstruction();
			case BITWISE_AND -> new BitwiseAndInstruction();
			case BITWISE_OR -> new BitwiseOrInstruction();
			case BITWISE_XOR -> new BitwiseXorInstruction();
			case CAST_INTEGER -> throw new AssertionError();
			case CAST_BOOLEAN -> throw new AssertionError();
			case CAST_STRING -> throw new AssertionError();
			case CAST_POINTER -> throw new AssertionError();
			case DUMP -> new DumpInstruction();
			case POP -> new PopInstruction();
			case DUP -> new DupInstruction();
			case SWAP -> new SwapInstruction();
			case OVER -> new OverInstruction();
			case ROT -> new RotInstruction();
		});
	}
}