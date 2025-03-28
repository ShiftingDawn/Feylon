use crate::linker;
use crate::linker::LinkedTokenData;
use crate::tokenizer;
use crate::tokenizer::Intrinsic;

const MEM_OFFSET: usize = 65536;

pub fn simulate_tokens(linker_context: linker::LinkerContext) {
    let mut stack: Vec<u32> = vec![];
    let mut memory: [u8; 131072] = [0; 131072];
    let mut string_ptr = 0;
    let mut program_counter: usize = 0;

    while program_counter < linker_context.result.len() {
        let op = &linker_context.result[program_counter];
        match &op.op {
            tokenizer::Op::PushInt(x) => {
                stack.push(*x);
                program_counter += 1;
            }
            tokenizer::Op::PushBool(x) => {
                stack.push(if *x { 1 } else { 0 });
                program_counter += 1;
            }
            tokenizer::Op::PushString(x) => {
                let str_as_chars = x.as_bytes();
                let strlen = str_as_chars.len();
                let addr = MEM_OFFSET + string_ptr;
                memory[addr..addr + strlen].copy_from_slice(str_as_chars);
                stack.push(strlen as u32);
                stack.push(addr as u32);
                string_ptr += strlen;
                program_counter += 1;
            }
            tokenizer::Op::Intrinsic(intrinsic) => {
                match intrinsic {
                    Intrinsic::Dump => {
                        print!("{}", stack.pop().unwrap());
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
                    Intrinsic::Mem => {
                        stack.push(0);
                    }
                    Intrinsic::MemSet => {
                        let a = stack.pop().unwrap();
                        let ptr = stack.pop().unwrap();
                        memory[ptr as usize] = a as u8;
                    }
                    Intrinsic::MemGet => {
                        let ptr = stack.pop().unwrap();
                        let x = memory[ptr as usize];
                        stack.push(x as u32);
                    }
                }
                program_counter += 1;
            }
            tokenizer::Op::IF => {
                let flag = stack.pop().unwrap();
                if flag == 0 {
                    match op.data {
                        LinkedTokenData::JumpAddr(ptr) => {
                            program_counter = ptr;
                        }
                        LinkedTokenData::None => panic!(),
                    }
                    continue;
                }
                program_counter += 1;
            }
            tokenizer::Op::ELSE => {
                match op.data {
                    LinkedTokenData::JumpAddr(ptr) => {
                        program_counter = ptr;
                    }
                    LinkedTokenData::None => panic!(),
                }
                continue;
            }
            tokenizer::Op::END => panic!(),
        }
    }
}
