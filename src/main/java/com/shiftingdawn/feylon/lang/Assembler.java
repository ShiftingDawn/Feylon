package com.shiftingdawn.feylon.lang;

import com.shiftingdawn.feylon.OrderedList;
import com.shiftingdawn.feylon.ins.*;

final class Assembler {

	public static AssembledProgram assemble(final LinkerContext linkerContext) {
		final OrderedList<LinkedToken> tokens = new OrderedList<>(linkerContext.result).reverse();
		final AssemblerContext ctx = new AssemblerContext();
		final int skippedPointerOffset = 0;
		while (!tokens.isEmpty()) {
			final LinkedToken token = tokens.pop();
			switch (token.type) {
				case PUSH_INT -> ctx.result.append(StackInstructions.push((int) token.data));
				case PUSH_BOOL -> ctx.result.append(StackInstructions.push((boolean) token.data ? 1 : 0));
				case PUSH_STRING -> ctx.result.append(StackInstructions.push((String) token.data));
				case PUSH_POINTER -> ctx.result.append(StackInstructions.push((int) token.data));
				case INTRINSIC -> Assembler.processIntrinsic(ctx, token);

				case FUNCTION -> ctx.result.append(ControlFlowInstructions.jump((int) token.data - skippedPointerOffset));
				case CALL -> ctx.result.append(ControlFlowInstructions.call(linkerContext.functions.get(token.txt).pointer - skippedPointerOffset + 1, (token.selfPointer - skippedPointerOffset) + 1));
				case RETURN -> ctx.result.append(ControlFlowInstructions::ret);

				case JUMP -> ctx.result.append(ControlFlowInstructions.jump((int) token.data - skippedPointerOffset));
				case JUMP_EQ -> ctx.result.append(ControlFlowInstructions.jumpEq((int) token.data - skippedPointerOffset));
				case JUMP_NEQ -> ctx.result.append(ControlFlowInstructions.jumpNeq((int) token.data - skippedPointerOffset));
				case DO -> ctx.result.append(ControlFlowInstructions.jumpNeq((int) token.data - skippedPointerOffset));

				default -> throw new AssertionError("Encountered unhandled token: " + token.type);
			}
		}
		return new AssembledProgram(ctx.result.toArray(Instruction[]::new), linkerContext.memSize);
	}

	private static void processIntrinsic(final AssemblerContext ctx, final LinkedToken token) {
		ctx.result.append(switch ((Intrinsics) token.data) {
			case ADD -> (Instruction) ArithmeticInstructions::add;
			case SUBTRACT -> (Instruction) ArithmeticInstructions::subtract;
			case MULTIPLY -> (Instruction) ArithmeticInstructions::multiply;
			case DIVIDE -> (Instruction) ArithmeticInstructions::divide;
			case MODULO -> (Instruction) ArithmeticInstructions::mod;
			case SHIFT_LEFT -> (Instruction) ArithmeticInstructions::bitShiftLeft;
			case SHIFT_RIGHT -> (Instruction) ArithmeticInstructions::bitShiftRight;
			case BITWISE_AND -> (Instruction) ArithmeticInstructions::bitwiseAnd;
			case BITWISE_OR -> (Instruction) ArithmeticInstructions::bitwiseOr;
			case BITWISE_XOR -> (Instruction) ArithmeticInstructions::bitwiseXor;

			case EQUALS -> (Instruction) ArithmeticInstructions::equals;
			case NOT_EQUALS -> (Instruction) ArithmeticInstructions::notEquals;
			case LESS -> (Instruction) ArithmeticInstructions::less;
			case GREATER -> (Instruction) ArithmeticInstructions::greater;
			case LESS_OR_EQUAL -> (Instruction) ArithmeticInstructions::lessEqual;
			case GREATER_OR_EQUAL -> (Instruction) ArithmeticInstructions::greaterEqual;

			case DUMP -> (Instruction) StackInstructions::dump;
			case POP -> (Instruction) StackInstructions::pop;
			case DUP -> (Instruction) StackInstructions::dup;
			case SWAP -> (Instruction) StackInstructions::swap;
			case OVER -> (Instruction) StackInstructions::over;
			case ROT -> (Instruction) StackInstructions::rot;

			case STORE -> (Instruction) MemoryInstructions::store8;
			case LOAD -> (Instruction) MemoryInstructions::load8;
			case STORE_16 -> (Instruction) MemoryInstructions::store16;
			case LOAD_16 -> (Instruction) MemoryInstructions::load16;
			case STORE_32 -> (Instruction) MemoryInstructions::store32;
			case LOAD_32 -> (Instruction) MemoryInstructions::load32;
		});
	}
}