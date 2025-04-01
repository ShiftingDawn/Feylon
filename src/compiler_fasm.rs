use crate::linker::{Instruction, LinkerContext};
use crate::tokenizer::Intrinsic;
use crate::{compiler_string, linker};
use std::io::Write;

pub fn process_program(file_path: &str, ctx: &LinkerContext) {
    let output_file_path = format!("{}.fasm", file_path);
    let mut out_file = std::fs::File::create(&output_file_path).unwrap_or_else(|e| {
        eprintln!("ERROR: Could not open file for compilation: {}", e);
        std::process::exit(1);
    });
    writeln!(&mut out_file, "format ELF64 executable 3").unwrap();
    writeln!(&mut out_file, "segment readable executable").unwrap();
    writeln!(&mut out_file, "print:").unwrap();
    writeln!(&mut out_file, "    mov rax, 1").unwrap();
    writeln!(&mut out_file, "    mov rdi, 1").unwrap();
    for op in &ctx.result {
        writeln!(&mut out_file, "addr_{}:    ;{}", op.self_ptr, compiler_string::stringify_op(&op)).unwrap();
        match op.instruction {
            Instruction::PushInt(x) => {
                writeln!(&mut out_file, "    mov rax, {}", x).unwrap();
                writeln!(&mut out_file, "    push rax").unwrap();
            }
            Instruction::PushPtr(x) => {
                writeln!(&mut out_file, "    mov rax, {}", x).unwrap();
                writeln!(&mut out_file, "    push rax").unwrap();
            }
            Instruction::PushBool(x) => {
                writeln!(&mut out_file, "    mov rax, {}", if x { 1 } else { 0 }).unwrap();
                writeln!(&mut out_file, "    push rax").unwrap();
            }
            Instruction::PushString(_) => {}
            Instruction::Intrinsic(intrinsic) => match intrinsic {
                Intrinsic::Dump => {
                    writeln!(&mut out_file, "    pop rdi").unwrap();
                    writeln!(&mut out_file, "    call print").unwrap();
                }
                Intrinsic::Drop => {
                    writeln!(&mut out_file, "    pop rax").unwrap();
                }
                Intrinsic::Dup => {
                    writeln!(&mut out_file, "    pop rax").unwrap();
                    writeln!(&mut out_file, "    push rax").unwrap();
                    writeln!(&mut out_file, "    push rax").unwrap();
                }
                Intrinsic::Over => {
                    writeln!(&mut out_file, "    pop rax").unwrap();
                    writeln!(&mut out_file, "    pop rbx").unwrap();
                    writeln!(&mut out_file, "    push rbx").unwrap();
                    writeln!(&mut out_file, "    push rax").unwrap();
                    writeln!(&mut out_file, "    push rbx").unwrap();
                }
                Intrinsic::Swap => {
                    writeln!(&mut out_file, "    pop rax").unwrap();
                    writeln!(&mut out_file, "    pop rbx").unwrap();
                    writeln!(&mut out_file, "    push rax").unwrap();
                    writeln!(&mut out_file, "    push rbx").unwrap();
                }
                Intrinsic::Rot => {
                    writeln!(&mut out_file, "    pop rax").unwrap();
                    writeln!(&mut out_file, "    pop rbx").unwrap();
                    writeln!(&mut out_file, "    pop rcx").unwrap();
                    writeln!(&mut out_file, "    push rbx").unwrap();
                    writeln!(&mut out_file, "    push rax").unwrap();
                    writeln!(&mut out_file, "    push rcx").unwrap();
                }
                Intrinsic::Add => {
                    writeln!(&mut out_file, "    pop rax").unwrap();
                    writeln!(&mut out_file, "    pop rbx").unwrap();
                    writeln!(&mut out_file, "    add rax, rbx").unwrap();
                    writeln!(&mut out_file, "    push rax").unwrap();
                }
                Intrinsic::Subtract => {
                    writeln!(&mut out_file, "    pop rax").unwrap();
                    writeln!(&mut out_file, "    pop rbx").unwrap();
                    writeln!(&mut out_file, "    sub rbx, rax").unwrap();
                    writeln!(&mut out_file, "    push rbx").unwrap();
                }
                Intrinsic::Multiply => {
                    writeln!(&mut out_file, "    pop rax").unwrap();
                    writeln!(&mut out_file, "    pop rbx").unwrap();
                    writeln!(&mut out_file, "    mul rbx").unwrap();
                    writeln!(&mut out_file, "    push rax").unwrap();
                }
                Intrinsic::Divide => {
                    writeln!(&mut out_file, "    pop rbx").unwrap();
                    writeln!(&mut out_file, "    pop rax").unwrap();
                    writeln!(&mut out_file, "    div rbx").unwrap();
                    writeln!(&mut out_file, "    push rax").unwrap();
                }
                Intrinsic::Modulo => {
                    writeln!(&mut out_file, "    xor rdx, rdx").unwrap();
                    writeln!(&mut out_file, "    pop rbx").unwrap();
                    writeln!(&mut out_file, "    pop rax").unwrap();
                    writeln!(&mut out_file, "    div rbx").unwrap();
                    writeln!(&mut out_file, "    push rdx").unwrap();
                }
                Intrinsic::ShiftLeft => {
                    writeln!(&mut out_file, "    pop rcx").unwrap();
                    writeln!(&mut out_file, "    pop rbx").unwrap();
                    writeln!(&mut out_file, "    shl rbx, cl").unwrap();
                    writeln!(&mut out_file, "    push rbx").unwrap();
                }
                Intrinsic::ShiftRight => {
                    writeln!(&mut out_file, "    pop rcx").unwrap();
                    writeln!(&mut out_file, "    pop rbx").unwrap();
                    writeln!(&mut out_file, "    shr rbx, cl").unwrap();
                    writeln!(&mut out_file, "    push rbx").unwrap();
                }
                Intrinsic::BitAnd => {
                    writeln!(&mut out_file, "    pop rax").unwrap();
                    writeln!(&mut out_file, "    pop rbx").unwrap();
                    writeln!(&mut out_file, "    and rbx, rax").unwrap();
                    writeln!(&mut out_file, "    push rbx").unwrap();
                }
                Intrinsic::BitOr => {
                    writeln!(&mut out_file, "    pop rax").unwrap();
                    writeln!(&mut out_file, "    pop rbx").unwrap();
                    writeln!(&mut out_file, "    or rbx, rax").unwrap();
                    writeln!(&mut out_file, "    push rbx").unwrap();
                }
                Intrinsic::BitXor => {
                    writeln!(&mut out_file, "    pop rax").unwrap();
                    writeln!(&mut out_file, "    pop rbx").unwrap();
                    writeln!(&mut out_file, "    xor rbx, rax").unwrap();
                    writeln!(&mut out_file, "    push rbx").unwrap();
                }
                Intrinsic::Equals => {
                    writeln!(&mut out_file, "    mov rcx, 0").unwrap();
                    writeln!(&mut out_file, "    mov rdx, 1").unwrap();
                    writeln!(&mut out_file, "    pop rax").unwrap();
                    writeln!(&mut out_file, "    pop rbx").unwrap();
                    writeln!(&mut out_file, "    cmp rax, rbx").unwrap();
                    writeln!(&mut out_file, "    cmove rcx, rdx").unwrap();
                    writeln!(&mut out_file, "    push rcx").unwrap();
                }
                Intrinsic::NotEquals => {
                    writeln!(&mut out_file, "    mov rcx, 0").unwrap();
                    writeln!(&mut out_file, "    mov rdx, 1").unwrap();
                    writeln!(&mut out_file, "    pop rax").unwrap();
                    writeln!(&mut out_file, "    pop rbx").unwrap();
                    writeln!(&mut out_file, "    cmp rax, rbx").unwrap();
                    writeln!(&mut out_file, "    cmovne rcx, rdx").unwrap();
                    writeln!(&mut out_file, "    push rcx").unwrap();
                }
                Intrinsic::Less => {
                    writeln!(&mut out_file, "    mov rcx, 0").unwrap();
                    writeln!(&mut out_file, "    mov rdx, 1").unwrap();
                    writeln!(&mut out_file, "    pop rax").unwrap();
                    writeln!(&mut out_file, "    pop rbx").unwrap();
                    writeln!(&mut out_file, "    cmp rax, rbx").unwrap();
                    writeln!(&mut out_file, "    cmovl rcx, rdx").unwrap();
                    writeln!(&mut out_file, "    push rcx").unwrap();
                }
                Intrinsic::Greater => {
                    writeln!(&mut out_file, "    mov rcx, 0").unwrap();
                    writeln!(&mut out_file, "    mov rdx, 1").unwrap();
                    writeln!(&mut out_file, "    pop rax").unwrap();
                    writeln!(&mut out_file, "    pop rbx").unwrap();
                    writeln!(&mut out_file, "    cmp rax, rbx").unwrap();
                    writeln!(&mut out_file, "    cmovg rcx, rdx").unwrap();
                    writeln!(&mut out_file, "    push rcx").unwrap();
                }
                Intrinsic::LessOrEqual => {
                    writeln!(&mut out_file, "    mov rcx, 0").unwrap();
                    writeln!(&mut out_file, "    mov rdx, 1").unwrap();
                    writeln!(&mut out_file, "    pop rax").unwrap();
                    writeln!(&mut out_file, "    pop rbx").unwrap();
                    writeln!(&mut out_file, "    cmp rax, rbx").unwrap();
                    writeln!(&mut out_file, "    cmovle rcx, rdx").unwrap();
                    writeln!(&mut out_file, "    push rcx").unwrap();
                }
                Intrinsic::GreaterOrEqual => {
                    writeln!(&mut out_file, "    mov rcx, 0").unwrap();
                    writeln!(&mut out_file, "    mov rdx, 1").unwrap();
                    writeln!(&mut out_file, "    pop rax").unwrap();
                    writeln!(&mut out_file, "    pop rbx").unwrap();
                    writeln!(&mut out_file, "    cmp rax, rbx").unwrap();
                    writeln!(&mut out_file, "    cmovge rcx, rdx").unwrap();
                    writeln!(&mut out_file, "    push rcx").unwrap();
                }
                Intrinsic::Store8 => {}
                Intrinsic::Store16 => {}
                Intrinsic::Store32 => {}
                Intrinsic::Load8 => {}
                Intrinsic::Load16 => {}
                Intrinsic::Load32 => {}
            },
            Instruction::Function => match op.data {
                linker::LinkedTokenData::JumpAddr(ptr) => {
                    writeln!(&mut out_file, "    jmp addr_{}", ptr).unwrap();
                }
                _ => panic!(),
            },
            Instruction::Call => {}
            Instruction::Return => {}
            Instruction::PushVars => {}
            Instruction::PopVars => {}
            Instruction::ApplyVar => {}
            Instruction::Jump => match op.data {
                linker::LinkedTokenData::JumpAddr(ptr) => {
                    writeln!(&mut out_file, "    jmp addr_{}", ptr).unwrap();
                }
                _ => panic!(),
            },
            Instruction::JumpNeq => match op.data {
                linker::LinkedTokenData::JumpAddr(ptr) => {
                    writeln!(&mut out_file, "    pop rax").unwrap();
                    writeln!(&mut out_file, "    test rax, rax").unwrap();
                    writeln!(&mut out_file, "    jz addr_{}", ptr).unwrap();
                }
                _ => panic!(),
            },
            Instruction::Do => match op.data {
                linker::LinkedTokenData::JumpAddr(ptr) => {
                    writeln!(&mut out_file, "    pop rax").unwrap();
                    writeln!(&mut out_file, "    test rax, rax").unwrap();
                    writeln!(&mut out_file, "    jz addr_{}", ptr).unwrap();
                }
                _ => panic!(),
            },
        }
    }
    writeln!(&mut out_file, "addr_{}:", ctx.result.len()).unwrap();
    writeln!(&mut out_file, "entry start").unwrap();
    writeln!(&mut out_file, "start:").unwrap();
    writeln!(&mut out_file, "    jmp addr_0").unwrap();
    writeln!(&mut out_file, "segment readable writeable").unwrap();
    // writeln!(&mut out_file, "    mov rax, 60").unwrap();
    // writeln!(&mut out_file, "    xor rdi, rdi").unwrap();
    // writeln!(&mut out_file, "    syscall").unwrap();
    // writeln!(&mut out_file, "section '.idata' import data readable writeable").unwrap();
    // writeln!(&mut out_file, "    library kernel32, 'kernel32.dll'").unwrap();
    // writeln!(&mut out_file, "    import kernel32, ExitProcess, 'ExitProcess', GetStdHandle, 'GetStdHandle'").unwrap();
    out_file.flush().unwrap_or_else(|e| {
        eprintln!("ERROR: Could not open file for compilation: {}", e);
        std::process::exit(1);
    });
    println!("SUCCESS: Written compilation to: {}", output_file_path);
}
