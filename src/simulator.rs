use crate::linker;
use crate::linker::LinkedTokenData;
use crate::tokenizer::Intrinsic;

pub fn simulate_tokens(linker_context: linker::LinkerContext) {
    let mut stack: Vec<u64> = vec![];
    let mut vars: Vec<u64> = vec![];
    let mut call_stack: Vec<usize> = vec![];
    let mut mem: Vec<u64> = vec![0; linker_context.mem_size];
    let mut string_pool = [0; 65536];
    let mut string_ptr = 0;
    let mut program_counter: usize = 0;

    while program_counter < linker_context.result.len() {
        let op = &linker_context.result[program_counter];
        match &op.instruction {
            linker::Instruction::PushInt(x) => {
                stack.push(*x);
                program_counter += 1;
            }
            linker::Instruction::PushPtr(x) | linker::Instruction::PushMem(x) => {
                stack.push(*x as u64);
                program_counter += 1;
            }
            linker::Instruction::PushBool(x) => {
                stack.push(if *x { 1 } else { 0 });
                program_counter += 1;
            }
            linker::Instruction::PushString(x) => {
                let str_as_chars = x.as_bytes();
                let strlen = str_as_chars.len();
                string_pool[string_ptr..string_ptr + strlen].copy_from_slice(str_as_chars);
                stack.push(strlen as u64);
                stack.push(string_ptr as u64);
                string_ptr += strlen;
                program_counter += 1;
            }
            linker::Instruction::Intrinsic(intrinsic) => {
                match intrinsic {
                    Intrinsic::Dump => {
                        println!("{}", stack.pop().unwrap());
                    }
                    Intrinsic::Drop => {
                        stack.pop();
                    }
                    Intrinsic::Dup => {
                        let a = stack.last().unwrap().clone();
                        stack.push(a);
                    }
                    Intrinsic::Over => {
                        let a = stack[stack.len() - 2];
                        stack.push(a);
                    }
                    Intrinsic::Swap => {
                        let a = stack.pop().unwrap();
                        let b = stack.pop().unwrap();
                        stack.push(a);
                        stack.push(b);
                    }
                    Intrinsic::Rot => {
                        let a = stack.pop().unwrap();
                        let b = stack.pop().unwrap();
                        let c = stack.pop().unwrap();
                        stack.push(b);
                        stack.push(c);
                        stack.push(a);
                    }
                    Intrinsic::Add => {
                        let a = stack.pop().unwrap();
                        let b = stack.pop().unwrap();
                        stack.push(a + b);
                    }
                    Intrinsic::Subtract => {
                        let a = stack.pop().unwrap();
                        let b = stack.pop().unwrap();
                        stack.push(b - a);
                    }
                    Intrinsic::Multiply => {
                        let a = stack.pop().unwrap();
                        let b = stack.pop().unwrap();
                        stack.push(a * b);
                    }
                    Intrinsic::Divide => {
                        let a = stack.pop().unwrap();
                        let b = stack.pop().unwrap();
                        stack.push(b / a);
                    }
                    Intrinsic::Modulo => {
                        let a = stack.pop().unwrap();
                        let b = stack.pop().unwrap();
                        stack.push(b % a);
                    }
                    Intrinsic::ShiftLeft => {
                        let a = stack.pop().unwrap();
                        let b = stack.pop().unwrap();
                        stack.push(b << a);
                    }
                    Intrinsic::ShiftRight => {
                        let a = stack.pop().unwrap();
                        let b = stack.pop().unwrap();
                        stack.push(b >> a);
                    }
                    Intrinsic::BitAnd => {
                        let a = stack.pop().unwrap();
                        let b = stack.pop().unwrap();
                        stack.push(b & a);
                    }
                    Intrinsic::BitOr => {
                        let a = stack.pop().unwrap();
                        let b = stack.pop().unwrap();
                        stack.push(b | a);
                    }
                    Intrinsic::BitXor => {
                        let a = stack.pop().unwrap();
                        let b = stack.pop().unwrap();
                        stack.push(b ^ a);
                    }
                    Intrinsic::Equals => {
                        let a = stack.pop().unwrap();
                        let b = stack.pop().unwrap();
                        stack.push(if a == b { 1 } else { 0 });
                    }
                    Intrinsic::NotEquals => {
                        let a = stack.pop().unwrap();
                        let b = stack.pop().unwrap();
                        stack.push(if a != b { 1 } else { 0 });
                    }
                    Intrinsic::Less => {
                        let a = stack.pop().unwrap();
                        let b = stack.pop().unwrap();
                        stack.push(if b < a { 1 } else { 0 });
                    }
                    Intrinsic::Greater => {
                        let a = stack.pop().unwrap();
                        let b = stack.pop().unwrap();
                        stack.push(if b > a { 1 } else { 0 });
                    }
                    Intrinsic::LessOrEqual => {
                        let a = stack.pop().unwrap();
                        let b = stack.pop().unwrap();
                        stack.push(if b <= a { 1 } else { 0 });
                    }
                    Intrinsic::GreaterOrEqual => {
                        let a = stack.pop().unwrap();
                        let b = stack.pop().unwrap();
                        stack.push(if b >= a { 1 } else { 0 });
                    }
                    Intrinsic::Store8 | Intrinsic::Store16 | Intrinsic::Store32 | Intrinsic::Store64 => {
                        let ptr = stack.pop().unwrap() as usize;
                        let a = stack.pop().unwrap();
                        mem[ptr] = a;
                    }
                    Intrinsic::Load8 | Intrinsic::Load16 | Intrinsic::Load32 | Intrinsic::Load64 => {
                        let ptr = stack.pop().unwrap() as usize;
                        let x = mem[ptr];
                        stack.push(x);
                    }
                }
                program_counter += 1;
            }
            linker::Instruction::Call => match op.data {
                LinkedTokenData::JumpAddr(ptr) => {
                    call_stack.push(program_counter + 1);
                    program_counter = ptr;
                }
                _ => panic!(),
            },
            linker::Instruction::Return => {
                let return_ptr = call_stack.pop().unwrap();
                program_counter = return_ptr;
            }
            linker::Instruction::PushVars => match op.data {
                LinkedTokenData::Count(count) => {
                    for _ in 0..count {
                        let x = stack.pop().unwrap();
                        vars.push(x);
                    }
                    program_counter += 1;
                }
                _ => panic!(),
            },
            linker::Instruction::ApplyVar => match op.data {
                LinkedTokenData::Index(var_index) => {
                    let x = vars[vars.len() - 1 - var_index];
                    stack.push(x);
                    program_counter += 1;
                }
                _ => panic!(),
            },
            linker::Instruction::PopVars => match op.data {
                LinkedTokenData::Count(count) => {
                    for _ in 0..count {
                        vars.pop();
                    }
                    program_counter += 1;
                }
                _ => panic!(),
            },
            linker::Instruction::Function => {
                program_counter += 1;
            }
            linker::Instruction::Jump => match op.data {
                LinkedTokenData::JumpAddr(ptr) => {
                    program_counter = ptr;
                }
                _ => panic!(),
            },
            linker::Instruction::JumpNeq => {
                let flag = stack.pop().unwrap();
                if flag == 0 {
                    match op.data {
                        LinkedTokenData::JumpAddr(ptr) => {
                            program_counter = ptr;
                        }
                        _ => panic!(),
                    }
                    continue;
                }
                program_counter += 1;
            }
            linker::Instruction::Do => {
                let flag = stack.pop().unwrap();
                if flag == 0 {
                    match op.data {
                        LinkedTokenData::JumpAddr(ptr) => {
                            program_counter = ptr;
                        }
                        _ => panic!(),
                    }
                    continue;
                }
                program_counter += 1;
            }
        }
    }

    if !stack.is_empty() {
        eprintln!("ERROR: Simulation exited with leftover data on the stack");
        eprintln!("ERROR: This may be a false positive if allowed_overflow has been used.");
        eprintln!("{:?}", stack);
    }
}
