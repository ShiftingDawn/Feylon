use crate::lexer;
use std::fmt::{Display, Formatter};

pub struct Token {
    pub word: lexer::Word,
    pub op: Op,
}

pub enum Op {
    PushInt(u32),
    PushString(String),

    Intrinsic(Intrinsic),

    END,
    IF,
    ELSE,
}

pub enum Intrinsic {
    Dump,

    Add,
    Subtract,
    Multiply,
    Divide,
    Modulo,

    Mem,
    MemSet,
    MemGet,
}

impl Display for Op {
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
        let txt = match self {
            Op::PushInt(_) => "PUSH_INT",
            Op::PushString(_) => "PUSH_STRING",

            Op::Intrinsic(_) => "INTRINSIC",

            Op::END => "END",
            Op::IF => "IF",
            Op::ELSE => "ELSE",
        };
        write!(f, "{}", txt)
    }
}

impl Display for Intrinsic {
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
        let txt = match self {
            Intrinsic::Dump => "DUMP",
            Intrinsic::Add => "ADD",
            Intrinsic::Subtract => "SUBTRACT",
            Intrinsic::Multiply => "MULTIPLY",
            Intrinsic::Divide => "DIVIDE",
            Intrinsic::Modulo => "MODULO",
            Intrinsic::Mem => "MEM",

            Intrinsic::MemSet => "MEMSET",
            Intrinsic::MemGet => "MEMGET",
        };
        write!(f, "{}", txt)
    }
}

pub fn parse_words_into_tokens(words: Vec<lexer::Word>) -> Vec<Token> {
    let mut result = vec![];
    for word in words {
        match word.txt.parse::<u32>() {
            Ok(x) => {
                result.push(Token { word, op: Op::PushInt(x) });
                continue;
            }
            Err(_) => {}
        }
        if word.txt.starts_with('"') && word.txt.ends_with('"') {
            let content = word.txt.clone();
            result.push(Token {
                word,
                op: Op::PushString(String::from(&content[1..content.len() - 1])),
            });
            continue;
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
        match get_operation_by_word(&word.txt) {
            Some(op) => {
                result.push(Token { word, op });
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
        "-" => Some(Intrinsic::Subtract),
        "*" => Some(Intrinsic::Multiply),
        "/" => Some(Intrinsic::Divide),
        "%" => Some(Intrinsic::Modulo),

        "mem" => Some(Intrinsic::Mem),
        "memset" => Some(Intrinsic::MemSet),
        "memget" => Some(Intrinsic::MemGet),

        _ => None,
    }
}

fn get_operation_by_word(word: &str) -> Option<Op> {
    match word {
        "end" => Some(Op::END),
        "if" => Some(Op::IF),
        "else" => Some(Op::ELSE),
        _ => None,
    }
}
