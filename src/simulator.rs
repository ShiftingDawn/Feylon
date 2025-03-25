use crate::tokenizer;

pub fn simulate_tokens(tokens: Vec<tokenizer::Token>) {
    let mut stack: Vec<i32> = vec![];
    let mut ptr: usize = 0;
    while ptr < tokens.len() {
        let op = &tokens[ptr];
        match &op.op {
            tokenizer::Op::PushInt(x) => {
                stack.push(*x);
                ptr += 1;
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
                }
                ptr += 1;
            }
        }
    }
}
