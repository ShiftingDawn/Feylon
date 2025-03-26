use crate::tokenizer;

const MEM_OFFSET: usize = 65536;

pub fn simulate_tokens(tokens: Vec<tokenizer::Token>) {
    let mut stack: Vec<i32> = vec![];
    let mut memory = [0; 131072];
    let mut string_ptr = 0;
    let mut program_counter: usize = 0;
    while program_counter < tokens.len() {
        let op = &tokens[program_counter];
        match &op.op {
            tokenizer::Op::PushInt(x) => {
                stack.push(*x);
                program_counter += 1;
            }
            tokenizer::Op::PushString(x) => {
                let strlen = (*x).len();
                stack.push(strlen as i32);
                let addr = MEM_OFFSET + string_ptr;
                stack.push(addr as i32);
                let str_as_chars: Vec<i32> = x.chars().map(|c| c as i32).collect();
                memory[addr..addr + str_as_chars.len()].copy_from_slice(&str_as_chars);
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
                        memory[ptr as usize] = a;
                    }
                    tokenizer::Intrinsic::MemGet => {
                        let ptr = stack.pop().unwrap();
                        let x = memory[ptr as usize];
                        stack.push(x);
                    }
                }
                program_counter += 1;
            }
        }
    }
}
