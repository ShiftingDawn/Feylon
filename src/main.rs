use crate::DataType::INT;
use std::cmp::PartialEq;
use std::fmt::{Debug, Display, Formatter, Write};

fn main() {
    let words = parse_line_into_words(String::from("<generated>"), 0, "1 dump");
    let tokens = parse_words_into_tokens(words);
    check_types(&tokens, 0);
}

fn find_char<F>(line: &str, start_pos: usize, predicate: F) -> usize
where
    F: Fn(char) -> bool,
{
    let mut pos = start_pos;
    while pos < line.len() && !predicate(line.chars().nth(pos).unwrap()) {
        pos += 1;
    }
    pos
}

#[derive(Debug)]
struct Word {
    file: String,
    row: u32,
    col: u32,
    pub txt: String,
}
impl Display for Word {
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
        write!(f, "{}:{}:{}", self.file, self.row, self.col)
    }
}

impl Clone for Word {
    fn clone(&self) -> Self {
        Self {
            file: self.file.clone(),
            row: self.row,
            col: self.col,
            txt: self.txt.clone(),
        }
    }
}

fn parse_line_into_words(file: String, row: u32, line: &str) -> Vec<Word> {
    let mut result = vec![];
    let mut pos = find_char(line, 0, |x| !x.is_whitespace());
    while pos < line.len() {
        let end_pos = find_char(&line, pos + 1, |x| x.is_whitespace());
        result.push(Word {
            file: file.clone(),
            row,
            col: pos as u32,
            txt: line[pos..end_pos].to_string(),
        });
        pos = find_char(line, end_pos + 1, |x| !x.is_whitespace());
    }
    result
}

struct Token {
    word: Word,
    op: Op,
}

enum Op {
    PushInt(i32),

    Intrinsic(Intrinsic),
}

impl Display for Op {
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
        todo!()
    }
}

enum Intrinsic {
    Dump,

    Add,
}

fn parse_words_into_tokens(words: Vec<Word>) -> Vec<Token> {
    let mut result = vec![];
    for word in words {
        match word.txt.parse::<i32>() {
            Ok(x) => {
                result.push(Token {
                    word,
                    op: Op::PushInt(x),
                });
                continue;
            }
            Err(_) => {}
        }
        match get_intrinsic_by_word(&word.txt) {
            Some(intrinsic) => {
                result.push(Token {
                    word,
                    op: Op::Intrinsic(intrinsic),
                });
                continue;
            }
            None => {}
        }

        eprintln!("Error while parsing word: {:?}", word);
        std::process::exit(1);
    }
    result
}

fn get_intrinsic_by_word(word: &str) -> Option<Intrinsic> {
    match word {
        "dump" => Some(Intrinsic::Dump),

        "+" => Some(Intrinsic::Add),

        _ => None,
    }
}

#[derive(Clone, PartialEq, Debug)]
enum DataType {
    INT,
}

impl Display for DataType {
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
        write!(f, "{:?}", self)
    }
}

#[derive(Clone)]
struct TypedPos {
    word: Word,
    typ: DataType,
}

struct Context {
    stack: Vec<TypedPos>,
    ptr: usize,
    outs: Vec<TypedPos>,
}

struct Signature {
    ins: Vec<TypedPos>,
    outs: Vec<TypedPos>,
}

fn check_types(ops: &Vec<Token>, allowed_overflow: usize) {
    let mut contexts: Vec<Context> = vec![Context {
        stack: vec![],
        ptr: 0,
        outs: vec![],
    }];
    while !contexts.is_empty() {
        let ctx = contexts.last_mut().unwrap();
        if ctx.ptr >= ops.len() {
            check_outputs(ctx, allowed_overflow);
            contexts.pop();
            continue;
        }
        let op = &ops[ctx.ptr];
        match &op.op {
            Op::PushInt(_) => {
                ctx.stack.push(TypedPos {
                    word: op.word.clone(),
                    typ: DataType::INT,
                });
                ctx.ptr += 1;
            }
            Op::Intrinsic(intrinsic) => {
                match intrinsic {
                    Intrinsic::Dump => {
                        let a = check_arity(1, ctx, op);
                        check_signature(
                            &op,
                            ctx,
                            vec![Signature {
                                ins: a,
                                outs: vec![],
                            }],
                        );
                    }
                    Intrinsic::Add => {
                        let a = check_arity(2, ctx, op);
                        check_signature(
                            &op,
                            ctx,
                            vec![Signature {
                                ins: a,
                                outs: vec![TypedPos {
                                    word: op.word.clone(),
                                    typ: INT,
                                }],
                            }],
                        );
                    }
                };
                ctx.ptr += 1;
            }
        }
    }
}

fn check_arity(count: usize, ctx: &mut Context, op: &Token) -> Vec<TypedPos> {
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

fn check_signature(op: &Token, ctx: &mut Context, sigs: Vec<Signature>) {
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
            eprintln!(
                "{}: ERROR: Not enough arguments were provided for '{}' '{}'.",
                op.word, op.op, op.word.txt
            );
            eprintln!("{}: INFO: Missing arguments:", op.word);
            while !inputs.is_empty() {
                let missing = inputs.pop().unwrap();
                eprintln!("{}: INFO: {}", missing.word, missing.typ);
            }
            exit = true;
            continue;
        }
        ctx.stack.clear();
        for output_type in signature.outs {
            ctx.stack.push(output_type);
        }
        return;
    }
    if (exit) {
        std::process::exit(1);
    }
}

fn check_outputs(ctx: &mut Context, allowed_overflow: usize) {
    while !ctx.stack.is_empty() && !ctx.outs.is_empty() {
        let expected = ctx.outs.pop().unwrap();
        let actual = ctx.stack.pop().unwrap();
        if expected.typ != actual.typ {
            eprintln!(
                "{}: ERROR: Unexpected type '{}' placed on the stack.",
                actual.word, actual.typ
            );
            eprintln!(
                "{}: INFO: Expected type was '{}' was found here",
                expected.word, expected.typ
            );
            std::process::exit(1);
        }
    }
    if ctx.stack.len() - allowed_overflow > ctx.outs.len() {
        eprintln!(
            "{}: ERROR: Found unhandled data on the stack.",
            ctx.stack.last().unwrap().word
        );
        while !ctx.stack.is_empty() {
            let unexpected = ctx.stack.pop().unwrap();
            eprintln!("{}: INFO: Type '{}'", unexpected.word, unexpected.typ);
        }
        std::process::exit(1);
    } else if ctx.stack.len() < ctx.outs.len() {
        eprintln!(
            "{}: ERROR: Missing expected data on the stack:",
            ctx.outs.last().unwrap().word
        );
        while !ctx.outs.is_empty() {
            let missing = ctx.outs.pop().unwrap();
            eprintln!("{}: INFO: Type '{}'", missing.word, missing.typ);
        }
        std::process::exit(1);
    }
}
