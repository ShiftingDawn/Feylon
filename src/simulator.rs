use crate::linker;
use crate::tokenizer;

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
                    tokenizer::Intrinsic::Dump => {
                        print!("{}", stack.pop().unwrap());
                    }
                    tokenizer::Intrinsic::Add => {
                        let a = stack.pop().unwrap();
                        let b = stack.pop().unwrap();
                        stack.push(a + b);
                    }
                    tokenizer::Intrinsic::Subtract => {
                        let a = stack.pop().unwrap();
                        let b = stack.pop().unwrap();
                        stack.push(b - a);
                    }
                    tokenizer::Intrinsic::Multiply => {
                        let a = stack.pop().unwrap();
                        let b = stack.pop().unwrap();
                        stack.push(a * b);
                    }
                    tokenizer::Intrinsic::Divide => {
                        let a = stack.pop().unwrap();
                        let b = stack.pop().unwrap();
                        stack.push(b / a);
                    }
                    tokenizer::Intrinsic::Modulo => {
                        let a = stack.pop().unwrap();
                        let b = stack.pop().unwrap();
                        stack.push(b % a);
                    }
                    tokenizer::Intrinsic::Mem => {
                        stack.push(0);
                    }
                    tokenizer::Intrinsic::MemSet => {
                        let a = stack.pop().unwrap();
                        let ptr = stack.pop().unwrap();
                        memory[ptr as usize] = a as u8;
                    }
                    tokenizer::Intrinsic::MemGet => {
                        let ptr = stack.pop().unwrap();
                        let x = memory[ptr as usize];
                        stack.push(x as u32);
                    }
                }
                program_counter += 1;
            }
            tokenizer::Op::Keyword(keyword) => {
                todo!();
            }
        }
    }
}
