package com.shiftingdawn.feylon.syntax;

import com.shiftingdawn.feylon.Instruction;
import com.shiftingdawn.feylon.ins.*;
import com.shiftingdawn.feylon.ins.jump.*;
import com.shiftingdawn.feylon.ins.mem.MemGetInstruction;
import com.shiftingdawn.feylon.ins.mem.MemSetInstruction;
import com.shiftingdawn.feylon.ins.sys.SysCall3Instruction;

class Assembler {

	public static Instruction[] assemble(final Compiler.InstructionSource[] program) {
		final Instruction[] result = new Instruction[program.length];
		for (int pointer = 0; pointer < program.length; ++pointer) {
			result[pointer] = switch (program[pointer].type) {
				case PUSH_INT -> new PushIntInstruction((Integer) program[pointer].data);
				case PUSH_STRING -> new PushStringInstruction((String) program[pointer].data);
				case INTRINSIC -> switch ((Intrinsic) program[pointer].data) {
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
				case END -> new EndInstruction((Integer) program[pointer].data);
				case IF -> new IfInstruction((Integer) program[pointer].data);
				case ELSE -> new ElseInstruction((Integer) program[pointer].data);
				case WHILE -> new WhileInstruction();
				case DO -> new DoInstruction((Integer) program[pointer].data);

				case OPERATION -> switch ((Operations) program[pointer].data) {
					case NOOP -> new NoopInstruction();
					case POP -> new PopInstruction();
					case DUP -> new DupInstruction();
					case SYSCALL3 -> new SysCall3Instruction();
					case MEM -> new NoopInstruction();
					case MEMSET -> new MemSetInstruction();
					case MEMGET -> new MemGetInstruction();
					case PRINT -> new PrintInstruction();
				};
			};
		}
		return result;
	}
}
