use crate::linker::{Instruction, LinkerContext};
use crate::tokenizer::Intrinsic;
use crate::{add_or_replace_extension, compiler_string, linker};
use std::io::Write;

pub fn process_program(file_path: &str, ctx: &LinkerContext) {
    let output_file_path = add_or_replace_extension(file_path, "asm");
    let mut out_file = std::fs::File::create(&output_file_path).unwrap_or_else(|e| {
        eprintln!("ERROR: Could not open file for compilation: {}", e);
        std::process::exit(1);
    });
    writeln!(&mut out_file, "BITS 64").unwrap();
    writeln!(&mut out_file, "global _start").unwrap();
    writeln!(&mut out_file, "extern GetStdHandle").unwrap();
    writeln!(&mut out_file, "extern WriteFile").unwrap();
    writeln!(&mut out_file, "extern ExitProcess").unwrap();
    writeln!(&mut out_file, "section .data").unwrap();
    writeln!(&mut out_file, "    newline: db 13, 10, 0").unwrap();
    writeln!(&mut out_file, "section .bss").unwrap();
    writeln!(&mut out_file, "    itoabuffer: resb 10").unwrap();
    writeln!(&mut out_file, "    callstack_rsp: resq 1").unwrap();
    writeln!(&mut out_file, "    callstack: resb 65536").unwrap();
    writeln!(&mut out_file, "    callstack_top:").unwrap();
    writeln!(&mut out_file, "    mem: resb {}", ctx.mem_size).unwrap();
    writeln!(&mut out_file, "section .text").unwrap();
    writeln!(&mut out_file, "print:").unwrap();
    writeln!(&mut out_file, "    sub rsp, 40").unwrap();
    writeln!(&mut out_file, "    mov rax, rcx").unwrap();
    writeln!(&mut out_file, "    lea rdi, [itoabuffer + 10]").unwrap();
    writeln!(&mut out_file, "    mov rcx, 10").unwrap();
    writeln!(&mut out_file, "    xor r8, r8").unwrap();
    writeln!(&mut out_file, "    .divloop:").unwrap();
    writeln!(&mut out_file, "        xor rdx,rdx").unwrap();
    writeln!(&mut out_file, "        div rcx").unwrap();
    writeln!(&mut out_file, "        add dl, '0'").unwrap();
    writeln!(&mut out_file, "        dec rdi").unwrap();
    writeln!(&mut out_file, "        mov [rdi], dl").unwrap();
    writeln!(&mut out_file, "        inc r8").unwrap();
    writeln!(&mut out_file, "        test rax, rax").unwrap();
    writeln!(&mut out_file, "        jnz .divloop").unwrap();
    writeln!(&mut out_file, "    mov ecx, -11").unwrap();
    writeln!(&mut out_file, "    call GetStdHandle").unwrap();
    writeln!(&mut out_file, "    mov rbx, rax").unwrap();
    writeln!(&mut out_file, "    mov rcx, rbx").unwrap();
    writeln!(&mut out_file, "    mov rdx, rdi").unwrap();
    writeln!(&mut out_file, "    lea r9, [rsp + 32]").unwrap();
    writeln!(&mut out_file, "    mov qword [rsp + 32], 0").unwrap();
    writeln!(&mut out_file, "    call WriteFile").unwrap();
    writeln!(&mut out_file, "    mov rcx, rbx").unwrap();
    writeln!(&mut out_file, "    mov rdx, newline").unwrap();
    writeln!(&mut out_file, "    mov r8, 2").unwrap();
    writeln!(&mut out_file, "    lea r9, [rsp + 32]").unwrap();
    writeln!(&mut out_file, "    mov qword [rsp + 32], 0").unwrap();
    writeln!(&mut out_file, "    call WriteFile").unwrap();
    writeln!(&mut out_file, "    add rsp, 40").unwrap();
    writeln!(&mut out_file, "    ret").unwrap();
    writeln!(&mut out_file, "_start:").unwrap();
    writeln!(&mut out_file, "    mov rax, callstack_top").unwrap();
    writeln!(&mut out_file, "    mov [callstack_rsp], rax").unwrap();
    writeln!(&mut out_file, "    jmp addr_0").unwrap();
    for op in &ctx.result {
        writeln!(&mut out_file, "addr_{}:    ;{}", op.self_ptr, compiler_string::stringify_op(&op)).unwrap();
        match op.instruction {
            Instruction::PushInt(x) => {
                writeln!(&mut out_file, "    mov rax, {}", x).unwrap();
                writeln!(&mut out_file, "    push rax").unwrap();
            }
            Instruction::PushPtr(ptr) => {
                writeln!(&mut out_file, "    mov rax, {}", ptr).unwrap();
                writeln!(&mut out_file, "    push rax").unwrap();
            }
            Instruction::PushMem(offset) => {
                writeln!(&mut out_file, "    mov rax, mem").unwrap();
                writeln!(&mut out_file, "    add rax, {}", offset).unwrap();
                writeln!(&mut out_file, "    push rax").unwrap();
            }
            Instruction::PushBool(x) => {
                writeln!(&mut out_file, "    mov rax, {}", if x { 1 } else { 0 }).unwrap();
                writeln!(&mut out_file, "    push rax").unwrap();
            }
            Instruction::PushString(_) => {
                //TODO STRING
            }
            Instruction::Intrinsic(intrinsic) => match intrinsic {
                Intrinsic::Dump => {
                    writeln!(&mut out_file, "    pop rcx").unwrap();
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
                    writeln!(&mut out_file, "    xor rdx, rdx").unwrap();
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
                    writeln!(&mut out_file, "    pop rbx").unwrap();
                    writeln!(&mut out_file, "    pop rax").unwrap();
                    writeln!(&mut out_file, "    cmp rax, rbx").unwrap();
                    writeln!(&mut out_file, "    cmovl rcx, rdx").unwrap();
                    writeln!(&mut out_file, "    push rcx").unwrap();
                }
                Intrinsic::Greater => {
                    writeln!(&mut out_file, "    mov rcx, 0").unwrap();
                    writeln!(&mut out_file, "    mov rdx, 1").unwrap();
                    writeln!(&mut out_file, "    pop rbx").unwrap();
                    writeln!(&mut out_file, "    pop rax").unwrap();
                    writeln!(&mut out_file, "    cmp rax, rbx").unwrap();
                    writeln!(&mut out_file, "    cmovg rcx, rdx").unwrap();
                    writeln!(&mut out_file, "    push rcx").unwrap();
                }
                Intrinsic::LessOrEqual => {
                    writeln!(&mut out_file, "    mov rcx, 0").unwrap();
                    writeln!(&mut out_file, "    mov rdx, 1").unwrap();
                    writeln!(&mut out_file, "    pop rbx").unwrap();
                    writeln!(&mut out_file, "    pop rax").unwrap();
                    writeln!(&mut out_file, "    cmp rax, rbx").unwrap();
                    writeln!(&mut out_file, "    cmovle rcx, rdx").unwrap();
                    writeln!(&mut out_file, "    push rcx").unwrap();
                }
                Intrinsic::GreaterOrEqual => {
                    writeln!(&mut out_file, "    mov rcx, 0").unwrap();
                    writeln!(&mut out_file, "    mov rdx, 1").unwrap();
                    writeln!(&mut out_file, "    pop rbx").unwrap();
                    writeln!(&mut out_file, "    pop rax").unwrap();
                    writeln!(&mut out_file, "    cmp rax, rbx").unwrap();
                    writeln!(&mut out_file, "    cmovge rcx, rdx").unwrap();
                    writeln!(&mut out_file, "    push rcx").unwrap();
                }
                Intrinsic::Store8 => {
                    writeln!(&mut out_file, "    pop rax").unwrap();
                    writeln!(&mut out_file, "    pop rbx").unwrap();
                    writeln!(&mut out_file, "    mov byte [rax], bl").unwrap();
                }
                Intrinsic::Store16 => {
                    writeln!(&mut out_file, "    pop rax").unwrap();
                    writeln!(&mut out_file, "    pop rbx").unwrap();
                    writeln!(&mut out_file, "    mov word [rax], bx").unwrap();
                }
                Intrinsic::Store32 => {
                    writeln!(&mut out_file, "    pop rax").unwrap();
                    writeln!(&mut out_file, "    pop rbx").unwrap();
                    writeln!(&mut out_file, "    mov dword [rax], ebx").unwrap();
                }
                Intrinsic::Store64 => {
                    writeln!(&mut out_file, "    pop rax").unwrap();
                    writeln!(&mut out_file, "    pop rbx").unwrap();
                    writeln!(&mut out_file, "    mov qword [rax], rbx").unwrap();
                }
                Intrinsic::Load8 => {
                    writeln!(&mut out_file, "    pop rax").unwrap();
                    writeln!(&mut out_file, "    movzx rbx, byte [rax]").unwrap();
                    writeln!(&mut out_file, "    push rbx").unwrap();
                }
                Intrinsic::Load16 => {
                    writeln!(&mut out_file, "    pop rax").unwrap();
                    writeln!(&mut out_file, "    movzx rbx, word [rax]").unwrap();
                    writeln!(&mut out_file, "    push rbx").unwrap();
                }
                Intrinsic::Load32 => {
                    writeln!(&mut out_file, "    pop rax").unwrap();
                    writeln!(&mut out_file, "    mov ebx, [rax]").unwrap();
                    writeln!(&mut out_file, "    push rbx").unwrap();
                }
                Intrinsic::Load64 => {
                    writeln!(&mut out_file, "    pop rax").unwrap();
                    writeln!(&mut out_file, "    mov rbx,[rax]").unwrap();
                    writeln!(&mut out_file, "    push rbx").unwrap();
                }
            },
            Instruction::Function => {
                writeln!(&mut out_file, "    mov [callstack_rsp], rsp").unwrap();
                writeln!(&mut out_file, "    mov rsp, rax").unwrap();
            }
            Instruction::Call => match op.data {
                linker::LinkedTokenData::JumpAddr(ptr) => {
                    writeln!(&mut out_file, "    mov rax, rsp").unwrap();
                    writeln!(&mut out_file, "    mov rsp, [callstack_rsp]").unwrap();
                    writeln!(&mut out_file, "    call addr_{}", ptr).unwrap();
                    writeln!(&mut out_file, "    mov [callstack_rsp], rsp").unwrap();
                    writeln!(&mut out_file, "    mov rsp, rax").unwrap();
                }
                _ => panic!(),
            },
            Instruction::Return => {
                writeln!(&mut out_file, "    mov rax, rsp").unwrap();
                writeln!(&mut out_file, "    mov rsp, [callstack_rsp]").unwrap();
                writeln!(&mut out_file, "    ret").unwrap();
            }
            Instruction::PushVars => match op.data {
                linker::LinkedTokenData::Count(var_count) => {
                    writeln!(&mut out_file, "    mov rax, [callstack_rsp]").unwrap();
                    writeln!(&mut out_file, "    sub rax, {}", var_count * 8).unwrap();
                    writeln!(&mut out_file, "    mov [callstack_rsp], rax").unwrap();
                    for i in 0..var_count {
                        writeln!(&mut out_file, "    pop rbx").unwrap();
                        writeln!(&mut out_file, "    mov [rax + {}], rbx", (var_count - 1 - i) * 8).unwrap();
                    }
                }
                _ => panic!(),
            },
            Instruction::PopVars => match op.data {
                linker::LinkedTokenData::Count(var_count) => {
                    writeln!(&mut out_file, "    mov rax, [callstack_rsp]").unwrap();
                    writeln!(&mut out_file, "    add rax, {}", 8 * var_count).unwrap();
                    writeln!(&mut out_file, "    mov [callstack_rsp], rax").unwrap();
                }
                _ => panic!(),
            },
            Instruction::ApplyVar => match op.data {
                linker::LinkedTokenData::Index(var_index) => {
                    writeln!(&mut out_file, "    mov rax, [callstack_rsp]").unwrap();
                    writeln!(&mut out_file, "    add rax, {}", 8 * var_index).unwrap();
                    writeln!(&mut out_file, "    push qword [rax]").unwrap();
                }
                _ => panic!(),
            },
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
    writeln!(&mut out_file, "addr_exit:").unwrap();
    writeln!(&mut out_file, "    sub rsp, 8").unwrap();
    writeln!(&mut out_file, "    xor rcx, rcx").unwrap();
    writeln!(&mut out_file, "    call ExitProcess").unwrap();
    out_file.flush().unwrap_or_else(|e| {
        eprintln!("ERROR: Could not open file for compilation: {}", e);
        std::process::exit(1);
    });
    println!("SUCCESS: Written assembly to: {}", output_file_path);
    compile_obj_file(file_path);
    link_obj_file(file_path);
}

fn compile_obj_file(file_path: &str) {
    let asm_file_path = add_or_replace_extension(file_path, "asm");
    let obj_file_path = add_or_replace_extension(file_path, "obj");
    let cmd = std::process::Command::new("nasm")
        .arg(asm_file_path)
        .args(vec!["-fwin64", "-g", "-o"])
        .arg(&obj_file_path)
        .output()
        .unwrap_or_else(|err| {
            eprintln!("ERROR: Could not compile assembly: {}", err);
            std::process::exit(1);
        });
    if cmd.status.success() {
        println!("SUCCESS: Written compiled assembly to: {}", obj_file_path);
    } else {
        eprintln!("ERROR: Could not compile assembly");
        eprintln!("{}", String::from_utf8_lossy(&cmd.stderr));
        std::process::exit(1);
    }
}

fn link_obj_file(file_path: &str) {
    let obj_file_path = add_or_replace_extension(file_path, "obj");
    let exe_file_path = add_or_replace_extension(file_path, "exe");
    let cmd = std::process::Command::new("golink")
        .args(vec!["/console", "/entry", "_start", "/debug", "coff", "/fo"])
        .arg(&exe_file_path)
        .arg(obj_file_path)
        .arg("kernel32.dll")
        .output()
        .unwrap_or_else(|err| {
            if err.to_string() == "program not found" {
                eprintln!("ERROR: Could not make executable: GoLink.exe was not found in PATH");
            } else {
                eprintln!("ERROR: Could not make executable: {}", err);
            }
            std::process::exit(1);
        });
    if cmd.status.success() {
        println!("SUCCESS: Written executable to: {}", exe_file_path);
    } else {
        eprintln!("ERROR: Could not make executable");
        eprintln!("{}", String::from_utf8_lossy(&cmd.stdout));
        eprintln!("{}", String::from_utf8_lossy(&cmd.stderr));
        std::process::exit(1);
    }
}
