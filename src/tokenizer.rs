use crate::lexer;
use std::fmt::{Display, Formatter};

pub struct Token {
    pub word: lexer::Word,
    pub op: Op,
}

pub enum Op {
    PushInt(u32),
    PushBool(bool),
    PushString(String),

    Intrinsic(Intrinsic),

    End,
    If,
    Else,
    While,
    Do,
}

#[derive(Copy, Clone)]
pub enum Intrinsic {
    Dump,
    Drop,
    Dup,
    Over,
    Swap,
    Rot,
    Add,
    Subtract,
    Multiply,
    Divide,
    Modulo,
    ShiftLeft,
    ShiftRight,
    BitAnd,
    BitOr,
    BitXor,
    Equals,
    NotEquals,
    Less,
    Greater,
    LessOrEqual,
    GreaterOrEqual,
    Mem,
    MemSet,
    MemGet,
}

impl Display for Op {
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
        let txt = match self {
            Op::PushInt(_) => "PUSH_INT",
            Op::PushBool(_) => "PUSH_BOOL",
            Op::PushString(_) => "PUSH_STRING",

            Op::Intrinsic(_) => "INTRINSIC",

            Op::End => "END",
            Op::If => "IF",
            Op::Else => "ELSE",
            Op::While => "WHILE",
            Op::Do => "DO",
        };
        write!(f, "{}", txt)
    }
}

impl Display for Intrinsic {
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
        let txt = match self {
            Intrinsic::Dump => "DUMP",
            Intrinsic::Drop => "DROP",
            Intrinsic::Dup => "DUP",
            Intrinsic::Over => "OVER",
            Intrinsic::Swap => "SWAP",
            Intrinsic::Rot => "ROT",
            Intrinsic::Add => "ADD",
            Intrinsic::Subtract => "SUBTRACT",
            Intrinsic::Multiply => "MULTIPLY",
            Intrinsic::Divide => "DIVIDE",
            Intrinsic::Modulo => "MODULO",
            Intrinsic::ShiftLeft => "SHIFT_LEFT",
            Intrinsic::ShiftRight => "SHIFT_RIGHT",
            Intrinsic::BitAnd => "BIT_AND",
            Intrinsic::BitOr => "BIT_OR",
            Intrinsic::BitXor => "BIT_XOR",
            Intrinsic::Equals => "EQUALS",
            Intrinsic::NotEquals => "NOT_EQUALS",
            Intrinsic::Less => "LESS",
            Intrinsic::Greater => "GREATER",
            Intrinsic::LessOrEqual => "LESS_OR_EQUAL",
            Intrinsic::GreaterOrEqual => "GREATER_OR_EQUAL",
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
        if word.txt == "true" || word.txt == "false" {
            let value = word.txt == "true";
            result.push(Token { word, op: Op::PushBool(value) });
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
        "drop" => Some(Intrinsic::Drop),
        "dup" => Some(Intrinsic::Dup),
        "over" => Some(Intrinsic::Over),
        "swap" => Some(Intrinsic::Swap),
        "rot" => Some(Intrinsic::Rot),
        "+" => Some(Intrinsic::Add),
        "-" => Some(Intrinsic::Subtract),
        "*" => Some(Intrinsic::Multiply),
        "/" => Some(Intrinsic::Divide),
        "%" => Some(Intrinsic::Modulo),
        "<<" => Some(Intrinsic::ShiftLeft),
        ">>" => Some(Intrinsic::ShiftRight),
        "&" => Some(Intrinsic::BitAnd),
        "|" => Some(Intrinsic::BitOr),
        "^" => Some(Intrinsic::BitXor),
        "=" => Some(Intrinsic::Equals),
        "!=" => Some(Intrinsic::NotEquals),
        "<" => Some(Intrinsic::Less),
        ">" => Some(Intrinsic::Greater),
        "<=" => Some(Intrinsic::LessOrEqual),
        ">=" => Some(Intrinsic::GreaterOrEqual),
        "mem" => Some(Intrinsic::Mem),
        "memset" => Some(Intrinsic::MemSet),
        "memget" => Some(Intrinsic::MemGet),
        _ => None,
    }
}

fn get_operation_by_word(word: &str) -> Option<Op> {
    match word {
        "end" => Some(Op::End),
        "if" => Some(Op::If),
        "else" => Some(Op::Else),
        "while" => Some(Op::While),
        "do" => Some(Op::Do),
        _ => None,
    }
}
