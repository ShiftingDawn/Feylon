package com.shiftingdawn.feylon.syntax;

import com.shiftingdawn.feylon.Instruction;
import com.shiftingdawn.feylon.ins.AddInstruction;
import com.shiftingdawn.feylon.ins.BitShiftLeftInstruction;
import com.shiftingdawn.feylon.ins.BitShiftRightInstruction;
import com.shiftingdawn.feylon.ins.BitwiseAndInstruction;
import com.shiftingdawn.feylon.ins.BitwiseOrInstruction;
import com.shiftingdawn.feylon.ins.BitwiseXorInstruction;
import com.shiftingdawn.feylon.ins.DivideInstruction;
import com.shiftingdawn.feylon.ins.DumpInstruction;
import com.shiftingdawn.feylon.ins.DupInstruction;
import com.shiftingdawn.feylon.ins.EqualsInstruction;
import com.shiftingdawn.feylon.ins.GreaterEqualInstruction;
import com.shiftingdawn.feylon.ins.GreaterInstruction;
import com.shiftingdawn.feylon.ins.LessEqualInstruction;
import com.shiftingdawn.feylon.ins.LessInstruction;
import com.shiftingdawn.feylon.ins.ModInstruction;
import com.shiftingdawn.feylon.ins.MultiplyInstruction;
import com.shiftingdawn.feylon.ins.NoopInstruction;
import com.shiftingdawn.feylon.ins.NotEqualsInstruction;
import com.shiftingdawn.feylon.ins.OverInstruction;
import com.shiftingdawn.feylon.ins.PopInstruction;
import com.shiftingdawn.feylon.ins.PushBooleanInstruction;
import com.shiftingdawn.feylon.ins.PushIntInstruction;
import com.shiftingdawn.feylon.ins.PushStringInstruction;
import com.shiftingdawn.feylon.ins.RotInstruction;
import com.shiftingdawn.feylon.ins.SubtractInstruction;
import com.shiftingdawn.feylon.ins.SwapInstruction;
import com.shiftingdawn.feylon.ins.jump.CallFunctionInstruction;
import com.shiftingdawn.feylon.ins.jump.DoInstruction;
import com.shiftingdawn.feylon.ins.jump.ElseInstruction;
import com.shiftingdawn.feylon.ins.jump.FunctionInstruction;
import com.shiftingdawn.feylon.ins.jump.IfInstruction;
import com.shiftingdawn.feylon.ins.jump.JumpInstruction;
import com.shiftingdawn.feylon.ins.jump.ReturnInstruction;
import com.shiftingdawn.feylon.ins.jump.WhileInstruction;
import com.shiftingdawn.feylon.ins.mem.MemGetInstruction;
import com.shiftingdawn.feylon.ins.mem.MemInstruction;
import com.shiftingdawn.feylon.ins.mem.MemSetInstruction;
import com.shiftingdawn.feylon.ins.sys.SysCall3Instruction;
import com.shiftingdawn.feylon.lang.Intrinsics;

class Assembler {

	public static Instruction[] assemble(final CompilerContext compilerContext) {
		final Instruction[] result = new Instruction[compilerContext.instructions.size()];
		for (int pointer = 0; pointer < compilerContext.instructions.size(); ++pointer) {
			if (compilerContext.instructions.get(pointer)==null) {
				continue;
			}
			final Object data = compilerContext.instructions.get(pointer).data;
			result[pointer] = switch (compilerContext.instructions.get(pointer).type) {
				case PUSH_INT -> new PushIntInstruction((Integer) data);
				case PUSH_STRING -> new PushStringInstruction((String) data);
				case INTRINSIC -> switch ((Intrinsics) data) {
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

					default -> {
						if (((Intrinsics) data).hasInstruction) {
							throw new AssertionError("This should not be here");
						}
						yield null;
					}
				};
				case FUNCTION -> new FunctionInstruction((Integer) data);
				case CALL -> new CallFunctionInstruction(compilerContext.functions.get((String) data), pointer + 1);
				case RETURN -> new ReturnInstruction();
				case JUMP -> new JumpInstruction((Integer) data);
				case IF -> new IfInstruction((Integer) data);
				case ELSE -> new ElseInstruction((Integer) data);
				case WHILE -> new WhileInstruction();
				case DO -> new DoInstruction((Integer) data);

				case INSTRUCTION -> Operations.getByText((String) data).map(operation -> switch (operation) {
					case NOOP -> new NoopInstruction();
					case POP -> new PopInstruction();
					case DUP -> new DupInstruction();
					case SWAP -> new SwapInstruction();
					case OVER -> new OverInstruction();
					case ROT -> new RotInstruction();
					case SYSCALL3 -> new SysCall3Instruction();
					case MEM -> new MemInstruction();
					case MEMSET -> new MemSetInstruction();
					case MEMGET -> new MemGetInstruction();
					case DUMP -> new DumpInstruction();
				}).orElseThrow(() -> new AssertionError("Encountered unknown operation '%s'".formatted(data)));
			};
		}
		return result;
	}
}
