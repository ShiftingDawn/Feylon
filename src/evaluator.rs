use crate::{checker, tokenizer};

pub fn evaluate_tokens(ctx: &mut tokenizer::ParserContext) {
    let mut input_tokens = std::mem::take(&mut ctx.result);
    input_tokens.reverse();
    ctx.result = vec![];
    while let Some(token) = input_tokens.pop() {
        match &token.op {
            tokenizer::Op::Const(const_name) => {
                let const_val = evaluate_constant(&token, ctx, &mut input_tokens);
                ctx.constants.insert(const_name.clone(), const_val);
            }
            _ => ctx.result.push(token),
        }
    }
}

fn evaluate_constant(const_token: &tokenizer::Token, ctx: &mut tokenizer::ParserContext, tokens: &mut Vec<tokenizer::Token>) -> tokenizer::ConstDef {
    let mut stack: Vec<tokenizer::ConstDef> = vec![];
    while let Some(token) = tokens.pop() {
        match token.op {
            tokenizer::Op::End => break,
            tokenizer::Op::PushInt(val) => stack.push(tokenizer::ConstDef { typ: checker::DataType::INT, val }),
            tokenizer::Op::ConstRef(const_ref_name) => match ctx.constants.get(const_ref_name.as_str()) {
                Some(ref_def) => stack.push(tokenizer::ConstDef {
                    typ: ref_def.typ,
                    val: ref_def.val,
                }),
                None => {
                    eprintln!(
                        "{}: ERROR: Encountered constant reference '{}' before it was defined when evaluating constant",
                        token.word, const_ref_name
                    );
                    std::process::exit(1);
                }
            },
            tokenizer::Op::Intrinsic(intrinsic) => {
                let a = stack.pop().unwrap();
                let b = stack.pop().unwrap();
                if a.typ != checker::DataType::INT {
                    eprintln!("{}: ERROR: Encountered illegal data type '{}' when evaluating constant", token.word, a.typ);
                    std::process::exit(1);
                }
                if b.typ != checker::DataType::INT {
                    eprintln!("{}: ERROR: Encountered illegal data type '{}' when evaluating constant", token.word, b.typ);
                    std::process::exit(1);
                }
                match intrinsic {
                    tokenizer::Intrinsic::Add => stack.push(tokenizer::ConstDef {
                        typ: checker::DataType::INT,
                        val: a.val + b.val,
                    }),
                    tokenizer::Intrinsic::Subtract => stack.push(tokenizer::ConstDef {
                        typ: checker::DataType::INT,
                        val: b.val - a.val,
                    }),
                    tokenizer::Intrinsic::Multiply => stack.push(tokenizer::ConstDef {
                        typ: checker::DataType::INT,
                        val: a.val * b.val,
                    }),
                    _ => {
                        eprintln!("{}: ERROR: Encountered illegal intrinsic '{}' when evaluating constant", token.word, token.word.txt);
                        std::process::exit(1);
                    }
                }
            }
            _ => {
                eprintln!(
                    "{}: ERROR: Encountered illegal '{}' token '{}' when evaluating constant",
                    token.word, token.op, token.word.txt
                );
                std::process::exit(1);
            }
        }
    }
    if stack.len() != 1 {
        eprintln!("{}: ERROR: The value of a constant should evaluate to a single number", const_token.word);
        std::process::exit(1);
    }
    stack.pop().unwrap()
}
