package com.shiftingdawn.feylon;

import com.shiftingdawn.feylon.ins.*;
import com.shiftingdawn.feylon.ins.jump.*;
import com.shiftingdawn.feylon.ins.mem.MemGetInstruction;
import com.shiftingdawn.feylon.ins.mem.MemSetInstruction;
import com.shiftingdawn.feylon.ins.sys.SysCall3Instruction;

class Assembler {

	public static Instruction[] assemble(final ProgramTuple[] program) {
		final Instruction[] result = new Instruction[program.length];
		for (int pointer = 0; pointer < program.length; ++pointer) {
			final Instruction ins = switch (program[pointer].op) {
				case NOOP -> new NoopInstruction();
				case OP_PUSH_INT -> new PushIntInstruction((Integer) program[pointer].data);
				case OP_PUSH_STRING -> new PushStringInstruction((String) program[pointer].data);
				case OP_POP -> new PopInstruction();
				case OP_DUP -> new DupInstruction();
				case OP_SYSCALL3 -> new SysCall3Instruction();
				case OP_MEM -> new NoopInstruction();
				case OP_MEM_SET -> new MemSetInstruction();
				case OP_MEM_GET -> new MemGetInstruction();
				case OP_PRINT -> new PrintInstruction();
				case OP_EQUALS -> new EqualsInstruction();
				case OP_NOT_EQUALS -> new NotEqualsInstruction();
				case OP_LESS -> new LessInstruction();
				case OP_GREATER -> new GreaterInstruction();
				case OP_LESS_EQUAL -> new LessEqualInstruction();
				case OP_GREATER_EQUAL -> new GreaterEqualInstruction();
				case OP_ADD -> new AddInstruction();
				case OP_SUBTRACT -> new SubtractInstruction();
				case OP_MULTIPLY -> new MultiplyInstruction();
				case OP_DIVIDE -> new DivideInstruction();
				case OP_MOD -> new ModInstruction();
				case OP_END -> new EndInstruction((Integer) program[pointer].data);
				case OP_IF -> new IfInstruction((Integer) program[pointer].data);
				case OP_ELSE -> new ElseInstruction((Integer) program[pointer].data);
				case OP_WHILE -> new WhileInstruction();
				case OP_DO -> new DoInstruction((Integer) program[pointer].data);
			};
			result[pointer] = ins;
		}
		return result;
	}
}
