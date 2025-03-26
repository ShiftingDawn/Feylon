use std::fmt::{Display, Formatter};
use crate::lexer;

pub struct Token {
    pub word: lexer::Word,
    pub op: Op,
}

pub enum Op {
    PushInt(i32),

    Intrinsic(Intrinsic),
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
            
            Op::Intrinsic(_) => "INTRINSIC",
        };
        write!(f, "{}", txt)
    }
}

pub fn parse_words_into_tokens(words: Vec<lexer::Word>) -> Vec<Token> {
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
