package com.shiftingdawn.feylon.syntax;

import com.shiftingdawn.feylon.Instruction;
import com.shiftingdawn.feylon.ins.*;
import com.shiftingdawn.feylon.ins.jump.*;
import com.shiftingdawn.feylon.ins.mem.MemGetInstruction;
import com.shiftingdawn.feylon.ins.mem.MemInstruction;
import com.shiftingdawn.feylon.ins.mem.MemSetInstruction;
import com.shiftingdawn.feylon.ins.sys.SysCall3Instruction;

class Assembler {

	public static Instruction[] assemble(final Compiler.SourceStack sourceStack) {
		final Instruction[] result = new Instruction[sourceStack.sources().length];
		for (int pointer = 0; pointer < sourceStack.sources().length; ++pointer) {
			if (sourceStack.sources()[pointer] == null) {
				continue;
			}
			final Object data = sourceStack.sources()[pointer].data;
			final int finalPointer = pointer;
			result[pointer] = switch (sourceStack.sources()[pointer].type) {
				case PUSH_INT -> new PushIntInstruction((Integer) data);
				case PUSH_STRING -> new PushStringInstruction((String) data);
				case INTRINSIC -> switch ((Intrinsic) data) {
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
				};
				case FUNCTION -> new FunctionInstruction((Integer) data);
				case RETURN -> new ReturnInstruction();
				case END -> new EndInstruction((Integer) data);
				case IF -> new IfInstruction((Integer) data);
				case ELSE -> new ElseInstruction((Integer) data);
				case WHILE -> new WhileInstruction();
				case DO -> new DoInstruction((Integer) data);

				case OPERATION -> Operations.getByText((String) data).map(operation -> switch (operation) {
					case NOOP -> new NoopInstruction();
					case POP -> new PopInstruction();
					case DUP -> new DupInstruction();
					case DUP2 -> new Dup2Instruction();
					case SWAP -> new SwapInstruction();
					case SYSCALL3 -> new SysCall3Instruction();
					case MEM -> new MemInstruction();
					case MEMSET -> new MemSetInstruction();
					case MEMGET -> new MemGetInstruction();
					case PRINT -> new PrintInstruction();
				}).orElseGet(() -> {
					if (!sourceStack.functions().containsKey(data)) {
						throw new AssertionError("Encountered unknown operation '%s'".formatted(data));
					}
					return new CallFunctionInstruction(sourceStack.functions().get(data), finalPointer + 1);
				});
			};
		}
		return result;
	}
}
