use crate::{checker, lexer};
use std::collections::HashMap;
use std::fmt::{Display, Formatter};

pub struct ConstDef {
    pub typ: checker::DataType,
    pub val: u32,
}

pub struct ParserContext {
    pub result: Vec<Token>,
    pub constants: HashMap<String, ConstDef>,
    known_constants: Vec<String>,
    block_stack: Vec<usize>,
    current_block_id: usize,
}

pub struct Token {
    pub word: lexer::Word,
    pub op: Op,
}

pub enum Op {
    PushInt(u32),
    PushBool(bool),
    PushString(String),

    Intrinsic(Intrinsic),
    Const(String),
    ConstRef(String),

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
            Op::Const(_) => "CONST",
            Op::ConstRef(_) => "CONST_REF",

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

pub fn parse_words_into_tokens(mut words: Vec<lexer::Word>) -> ParserContext {
    words.reverse();
    let mut ctx = ParserContext {
        result: vec![],
        constants: HashMap::new(),
        known_constants: vec![],
        block_stack: vec![],
        current_block_id: 0,
    };
    while !words.is_empty() {
        let word = words.pop().unwrap();
        match word.txt.parse::<u32>() {
            Ok(x) => {
                ctx.result.push(Token { word, op: Op::PushInt(x) });
                continue;
            }
            Err(_) => {}
        }
        if word.txt.starts_with('"') && word.txt.ends_with('"') {
            let content = word.txt.clone();
            ctx.result.push(Token {
                word,
                op: Op::PushString(String::from(&content[1..content.len() - 1])),
            });
            continue;
        }
        //TODO imports here
        if "true" == word.txt || "false" == word.txt {
            let value = "true" == word.txt;
            ctx.result.push(Token { word, op: Op::PushBool(value) });
            continue;
        }
        match get_intrinsic_by_word(&word.txt) {
            Some(intrinsic) => {
                ctx.result.push(Token {
                    word,
                    op: Op::Intrinsic(intrinsic),
                });
                continue;
            }
            None => {}
        }
        match get_operation_by_word(&word.txt) {
            Some(op) => {
                match op {
                    Op::End => {
                        let last_block_id = ctx.block_stack.pop().unwrap();
                        //TODO remove variables here
                        ctx.result.push(Token { word, op });
                    }
                    Op::If => {
                        if words.is_empty() {
                            eprintln!("{}: ERROR: Encountered incomplete IF statement", word);
                            std::process::exit(1);
                        }
                        ctx.block_stack.push(ctx.current_block_id);
                        ctx.current_block_id += 1;
                        ctx.result.push(Token { word, op });
                    }
                    Op::Else => {
                        if words.is_empty() {
                            eprintln!("{}: ERROR: Encountered incomplete ELSE statement", word);
                            std::process::exit(1);
                        }
                        ctx.block_stack.push(ctx.current_block_id);
                        ctx.current_block_id += 1;
                        ctx.result.push(Token { word, op });
                    }
                    Op::While => {
                        if words.is_empty() {
                            eprintln!("{}: ERROR: Encountered incomplete WHILE statement", word);
                            std::process::exit(1);
                        }
                        ctx.result.push(Token { word, op });
                    }
                    Op::Do => {
                        if words.is_empty() {
                            eprintln!("{}: ERROR: Encountered incomplete DO statement", word);
                            std::process::exit(1);
                        }
                        ctx.block_stack.push(ctx.current_block_id);
                        ctx.current_block_id += 1;
                        ctx.result.push(Token { word, op });
                    }
                    _ => panic!("Encountered unhandled operation: {}", op),
                }
                continue;
            }
            None => {}
        }
        if "const" == word.txt {
            if words.is_empty() {
                eprintln!("{}: ERROR: Encountered incomplete constant", word);
                std::process::exit(1);
            }
            ctx.block_stack.push(ctx.current_block_id);
            ctx.current_block_id += 1;
            let const_name = words.pop().unwrap().txt;
            ctx.result.push(Token {
                word,
                op: Op::Const(const_name.clone()),
            });
            ctx.known_constants.push(const_name);
            continue;
        }
        if ctx.known_constants.contains(&word.txt) {
            let const_name = word.txt.clone();
            ctx.result.push(Token {
                word,
                op: Op::ConstRef(const_name),
            });
            continue;
        }
        eprintln!("Error while parsing word: {:?}", word);
        std::process::exit(1);
    }
    ctx
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
