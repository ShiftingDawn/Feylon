use crate::linker::{Instruction, LinkedTokenData};
use crate::{lexer, linker, tokenizer};
use std::collections::HashMap;
use std::fmt::{Display, Formatter};

#[derive(Clone, Copy, PartialEq, Debug)]
pub enum DataType {
    INT,
    PTR,
    BOOL,
}

pub fn get_data_type_by_text(txt: &str) -> Option<DataType> {
    match txt {
        "int" => Some(DataType::INT),
        "ptr" => Some(DataType::PTR),
        "bool" => Some(DataType::BOOL),
        _ => None,
    }
}

impl Display for DataType {
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
        write!(f, "{:?}", self)
    }
}

#[derive(Clone)]
pub struct TypedPos {
    pub word: lexer::Word,
    pub typ: DataType,
}

struct Context {
    stack: Vec<TypedPos>,
    ptr: usize,
    outs: Vec<TypedPos>,
}

#[derive(Clone)]
struct Signature {
    ins: Vec<TypedPos>,
    outs: Vec<TypedPos>,
}

pub fn check_types(linker_context: &linker::LinkerContext, allowed_overflow: usize) {
    let mut visited_loops: HashMap<usize, Vec<TypedPos>> = HashMap::new();
    let mut function_signatures: HashMap<String, Signature> = HashMap::new();
    for (func_name, func_ref) in &linker_context.functions {
        let sig = Signature {
            ins: func_ref.ins.clone(),
            outs: func_ref.outs.clone(),
        };
        function_signatures.insert(func_name.clone(), sig);
    }
    let ops = &linker_context.result;
    let mut contexts: Vec<Context> = vec![Context {
        stack: vec![],
        ptr: 0,
        outs: vec![],
    }];
    while !&contexts.is_empty() {
        let ctx = contexts.last_mut().unwrap();
        if ctx.ptr >= ops.len() {
            check_outputs(ctx, allowed_overflow);
            contexts.pop();
            continue;
        }
        let op = &ops[ctx.ptr];
        match &op.instruction {
            Instruction::PushInt(_) => {
                ctx.stack.push(TypedPos {
                    word: op.word.clone(),
                    typ: DataType::INT,
                });
                ctx.ptr += 1;
            }
            Instruction::PushPtr(_) => {
                ctx.stack.push(TypedPos {
                    word: op.word.clone(),
                    typ: DataType::PTR,
                });
                ctx.ptr += 1;
            }
            Instruction::PushString(_) => {
                ctx.stack.push(tp(&op.word, DataType::INT));
                ctx.stack.push(tp(&op.word, DataType::PTR));
                ctx.ptr += 1;
            }
            Instruction::PushBool(_) => {
                ctx.stack.push(TypedPos {
                    word: op.word.clone(),
                    typ: DataType::BOOL,
                });
                ctx.ptr += 1;
            }
            Instruction::Intrinsic(intrinsic) => {
                match intrinsic {
                    tokenizer::Intrinsic::Dump => {
                        let a = check_arity(1, ctx, op);
                        check_signature(&op, ctx, vec![Signature { ins: a, outs: vec![] }]);
                    }
                    tokenizer::Intrinsic::Drop => {
                        let a = check_arity(1, ctx, op);
                        check_signature(&op, ctx, vec![Signature { ins: a, outs: vec![] }]);
                    }
                    tokenizer::Intrinsic::Dup => {
                        let a = check_arity(1, ctx, op);
                        check_signature(
                            &op,
                            ctx,
                            vec![Signature {
                                ins: a.clone(),
                                outs: vec![a[0].clone(), a[0].clone()],
                            }],
                        );
                    }
                    tokenizer::Intrinsic::Over => {
                        let ab = check_arity(2, ctx, op);
                        check_signature(
                            &op,
                            ctx,
                            vec![Signature {
                                ins: ab.clone(),
                                outs: vec![ab[0].clone(), ab[1].clone(), ab[0].clone()],
                            }],
                        );
                    }
                    tokenizer::Intrinsic::Swap => {
                        let ab = check_arity(2, ctx, op);
                        check_signature(
                            &op,
                            ctx,
                            vec![Signature {
                                ins: ab.clone(),
                                outs: vec![ab[1].clone(), ab[0].clone()],
                            }],
                        );
                    }
                    tokenizer::Intrinsic::Rot => {
                        let abc = check_arity(3, ctx, op);
                        check_signature(
                            &op,
                            ctx,
                            vec![Signature {
                                ins: abc.clone(),
                                outs: vec![abc[1].clone(), abc[2].clone(), abc[0].clone()],
                            }],
                        );
                    }
                    tokenizer::Intrinsic::Add => {
                        let a = check_arity(2, ctx, op);
                        check_signature(
                            &op,
                            ctx,
                            vec![Signature {
                                ins: a,
                                outs: vec![tp(&op.word, DataType::INT)],
                            }],
                        );
                    }
                    tokenizer::Intrinsic::Subtract => {
                        let a = check_arity(2, ctx, op);
                        check_signature(
                            &op,
                            ctx,
                            vec![Signature {
                                ins: a,
                                outs: vec![tp(&op.word, DataType::INT)],
                            }],
                        );
                    }
                    tokenizer::Intrinsic::Multiply => {
                        let a = check_arity(2, ctx, op);
                        check_signature(
                            &op,
                            ctx,
                            vec![Signature {
                                ins: a,
                                outs: vec![tp(&op.word, DataType::INT)],
                            }],
                        );
                    }
                    tokenizer::Intrinsic::Divide => {
                        let a = check_arity(2, ctx, op);
                        check_signature(
                            &op,
                            ctx,
                            vec![Signature {
                                ins: a,
                                outs: vec![tp(&op.word, DataType::INT)],
                            }],
                        );
                    }
                    tokenizer::Intrinsic::Modulo => {
                        let a = check_arity(2, ctx, op);
                        check_signature(
                            &op,
                            ctx,
                            vec![Signature {
                                ins: a,
                                outs: vec![tp(&op.word, DataType::INT)],
                            }],
                        );
                    }
                    tokenizer::Intrinsic::ShiftLeft => {
                        let a = check_arity(2, ctx, op);
                        check_signature(
                            &op,
                            ctx,
                            vec![Signature {
                                ins: a,
                                outs: vec![tp(&op.word, DataType::INT)],
                            }],
                        );
                    }
                    tokenizer::Intrinsic::ShiftRight => {
                        let a = check_arity(2, ctx, op);
                        check_signature(
                            &op,
                            ctx,
                            vec![Signature {
                                ins: a,
                                outs: vec![tp(&op.word, DataType::INT)],
                            }],
                        );
                    }
                    tokenizer::Intrinsic::BitAnd => {
                        let a = check_arity(2, ctx, op);
                        check_signature(
                            &op,
                            ctx,
                            vec![Signature {
                                ins: a,
                                outs: vec![tp(&op.word, DataType::INT)],
                            }],
                        );
                    }
                    tokenizer::Intrinsic::BitOr => {
                        let a = check_arity(2, ctx, op);
                        check_signature(
                            &op,
                            ctx,
                            vec![Signature {
                                ins: a,
                                outs: vec![tp(&op.word, DataType::INT)],
                            }],
                        );
                    }
                    tokenizer::Intrinsic::BitXor => {
                        let a = check_arity(2, ctx, op);
                        check_signature(
                            &op,
                            ctx,
                            vec![Signature {
                                ins: a,
                                outs: vec![tp(&op.word, DataType::INT)],
                            }],
                        );
                    }
                    tokenizer::Intrinsic::Equals => {
                        let a = check_arity(2, ctx, op);
                        check_signature(
                            &op,
                            ctx,
                            vec![Signature {
                                ins: a,
                                outs: vec![tp(&op.word, DataType::BOOL)],
                            }],
                        );
                    }
                    tokenizer::Intrinsic::NotEquals => {
                        let a = check_arity(2, ctx, op);
                        check_signature(
                            &op,
                            ctx,
                            vec![Signature {
                                ins: a,
                                outs: vec![tp(&op.word, DataType::BOOL)],
                            }],
                        );
                    }
                    tokenizer::Intrinsic::Less => {
                        let a = check_arity(2, ctx, op);
                        check_signature(
                            &op,
                            ctx,
                            vec![Signature {
                                ins: a,
                                outs: vec![tp(&op.word, DataType::BOOL)],
                            }],
                        );
                    }
                    tokenizer::Intrinsic::Greater => {
                        let a = check_arity(2, ctx, op);
                        check_signature(
                            &op,
                            ctx,
                            vec![Signature {
                                ins: a,
                                outs: vec![tp(&op.word, DataType::BOOL)],
                            }],
                        );
                    }
                    tokenizer::Intrinsic::LessOrEqual => {
                        let a = check_arity(2, ctx, op);
                        check_signature(
                            &op,
                            ctx,
                            vec![Signature {
                                ins: a,
                                outs: vec![tp(&op.word, DataType::BOOL)],
                            }],
                        );
                    }
                    tokenizer::Intrinsic::GreaterOrEqual => {
                        let a = check_arity(2, ctx, op);
                        check_signature(
                            &op,
                            ctx,
                            vec![Signature {
                                ins: a,
                                outs: vec![tp(&op.word, DataType::BOOL)],
                            }],
                        );
                    }
                    tokenizer::Intrinsic::Store8 | tokenizer::Intrinsic::Store16 | tokenizer::Intrinsic::Store32 => {
                        check_signature(
                            &op,
                            ctx,
                            vec![Signature {
                                ins: vec![tp(&op.word, DataType::INT), tp(&op.word, DataType::PTR)],
                                outs: vec![],
                            }],
                        );
                    }
                    tokenizer::Intrinsic::Load8 | tokenizer::Intrinsic::Load16 | tokenizer::Intrinsic::Load32 => {
                        check_signature(
                            &op,
                            ctx,
                            vec![Signature {
                                ins: vec![tp(&op.word, DataType::PTR)],
                                outs: vec![tp(&op.word, DataType::INT)],
                            }],
                        );
                    }
                };
                ctx.ptr += 1;
            }
            Instruction::Function => match op.data {
                LinkedTokenData::JumpAddr(ptr) => ctx.ptr = ptr,
                LinkedTokenData::None => {
                    eprintln!("{}: ERROR: Missing 'end'", op.word);
                    std::process::exit(1);
                }
            },
            Instruction::Call => match function_signatures.get(&op.word.txt) {
                None => {}
                Some(sig) => {
                    check_signature(&op, ctx, vec![sig.clone()]);
                    ctx.ptr += 1;
                }
            },
            Instruction::Return => {
                check_outputs(ctx, 0);
                contexts.pop();
            }
            Instruction::JumpNeq => {
                check_signature(
                    &op,
                    ctx,
                    vec![Signature {
                        ins: vec![tp(&op.word, DataType::BOOL)],
                        outs: vec![],
                    }],
                );
                ctx.ptr += 1;
                match op.data {
                    LinkedTokenData::JumpAddr(ptr) => {
                        let new_ctx = Context {
                            stack: ctx.stack.clone(),
                            ptr,
                            outs: ctx.outs.clone(),
                        };
                        contexts.push(new_ctx);
                        continue;
                    }
                    LinkedTokenData::None => {
                        eprintln!("{}: ERROR: Missing 'end'", op.word);
                        std::process::exit(1);
                    }
                }
            }
            Instruction::Jump => match op.data {
                LinkedTokenData::JumpAddr(ptr) => ctx.ptr = ptr,
                LinkedTokenData::None => {
                    eprintln!("{}: ERROR: Missing 'end'", op.word);
                    std::process::exit(1);
                }
            },
            Instruction::Do => {
                check_signature(
                    &op,
                    ctx,
                    vec![Signature {
                        ins: vec![tp(&op.word, DataType::BOOL)],
                        outs: vec![],
                    }],
                );
                if !visited_loops.contains_key(&ctx.ptr) {
                    visited_loops.insert(ctx.ptr, ctx.stack.clone());
                    ctx.ptr += 1;
                    let jump_ptr = match op.data {
                        LinkedTokenData::JumpAddr(ptr) => ptr,
                        LinkedTokenData::None => {
                            eprintln!("{}: ERROR: Encountered 'do' without jump address. This is a linking error!", op.word);
                            std::process::exit(1);
                        }
                    };
                    let new_ctx = Context {
                        stack: ctx.stack.clone(),
                        ptr: jump_ptr,
                        outs: ctx.outs.clone(),
                    };
                    contexts.push(new_ctx);
                    continue;
                } else {
                    let expected_types: Vec<DataType> = visited_loops.get(&ctx.ptr).unwrap().iter().map(|x| x.typ.clone()).collect();
                    let actual_types: Vec<DataType> = ctx.stack.iter().map(|x| x.typ.clone()).collect();
                    if expected_types != actual_types {
                        eprintln!("{}: ERROR: Loops are not allowed to modify the stack between iterations!", op.word);
                        eprintln!("{}: INFO : Stack before loop:", op.word);
                        if visited_loops.get(&ctx.ptr).unwrap().is_empty() {
                            eprintln!("{}: INFO : <empty>", op.word);
                        } else {
                            let before_tokens = visited_loops.get(&ctx.ptr).unwrap();
                            for before_token in before_tokens {
                                eprintln!("{}: INFO : {}", before_token.word, before_token.typ);
                            }
                        }
                        std::process::exit(1);
                    }
                    contexts.pop();
                    continue;
                }
            }
        }
    }
}

fn check_arity(count: usize, ctx: &mut Context, op: &linker::LinkedToken) -> Vec<TypedPos> {
    if count > ctx.stack.len() {
        eprintln!(
            "{}: ERROR: Not enough arguments were provided for '{}'. Expected {} but got {}",
            op.word,
            op.word.txt,
            count,
            ctx.stack.len()
        );
        std::process::exit(1);
    }
    let mut result: Vec<TypedPos> = vec![];
    for i in 0..count {
        result.push(ctx.stack[ctx.stack.len() - 1 - i].clone());
    }
    result
}

fn check_signature(op: &linker::LinkedToken, ctx: &mut Context, sigs: Vec<Signature>) {
    let mut exit = false;
    'OUTER: for signature in sigs {
        let mut inputs = signature.ins;
        let mut stack: Vec<TypedPos> = ctx.stack.clone();
        let mut args = 0;
        while !inputs.is_empty() && !stack.is_empty() {
            let expected = inputs.pop().unwrap();
            let actual = stack.pop().unwrap();
            if expected.typ != actual.typ {
                eprintln!(
                    "{}: ERROR: Argument {} of {} is expected to be type '{}' but received type '{}' instead.",
                    op.word, args, op.word.txt, expected.typ, actual.typ
                );
                eprintln!("{}: INFO: Argument {} was found here", actual.word, args);
                eprintln!("{}: INFO: Expected argument is defined here", expected.word);
                exit = true;
                continue 'OUTER;
            }
            args += 1;
        }
        if stack.len() < inputs.len() {
            eprintln!("{}: ERROR: Not enough arguments were provided for '{}' '{}'.", op.word, op.instruction, op.word.txt);
            eprintln!("{}: INFO: Missing arguments:", op.word);
            while !inputs.is_empty() {
                let missing = inputs.pop().unwrap();
                eprintln!("{}: INFO: {}", missing.word, missing.typ);
            }
            exit = true;
            continue;
        }
        ctx.stack.clear();
        for stack_type in stack {
            ctx.stack.push(stack_type);
        }
        for output_type in signature.outs {
            ctx.stack.push(output_type);
        }
        return;
    }
    if exit {
        std::process::exit(1);
    }
}

fn check_outputs(ctx: &mut Context, allowed_overflow: usize) {
    while !ctx.stack.is_empty() && !ctx.outs.is_empty() {
        let expected = ctx.outs.pop().unwrap();
        let actual = ctx.stack.pop().unwrap();
        if expected.typ != actual.typ {
            eprintln!("{}: ERROR: Unexpected type '{}' placed on the stack.", actual.word, actual.typ);
            eprintln!("{}: INFO: Expected type was '{}' was found here", expected.word, expected.typ);
            std::process::exit(1);
        }
    }
    if ctx.stack.len() - allowed_overflow > ctx.outs.len() {
        eprintln!("{}: ERROR: Found unhandled data on the stack.", ctx.stack.last().unwrap().word);
        while !ctx.stack.is_empty() {
            let unexpected = ctx.stack.pop().unwrap();
            eprintln!("{}: INFO: Type '{}'", unexpected.word, unexpected.typ);
        }
        std::process::exit(1);
    } else if ctx.stack.len() < ctx.outs.len() {
        eprintln!("{}: ERROR: Missing expected data on the stack:", ctx.outs.last().unwrap().word);
        while !ctx.outs.is_empty() {
            let missing = ctx.outs.pop().unwrap();
            eprintln!("{}: INFO: Type '{}'", missing.word, missing.typ);
        }
        std::process::exit(1);
    }
}

fn tp(word: &lexer::Word, typ: DataType) -> TypedPos {
    TypedPos { word: word.clone(), typ }
}
