use crate::checker::TypedPos;
use crate::lexer::Word;
use crate::{checker, lexer};
use std::collections::HashMap;
use std::fmt::{Display, Formatter};

pub struct ConstDef {
    pub typ: checker::DataType,
    pub val: u32,
}

pub struct MemoryDef {
    pub ptr: usize,
    pub size: usize,
}

pub struct FunctionDef {
    pub ins: Vec<TypedPos>,
    pub outs: Vec<TypedPos>,
}

pub struct ParserContext {
    pub result: Vec<Token>,
    pub constants: HashMap<String, ConstDef>,
    pub functions: HashMap<String, FunctionDef>,
    pub memories: HashMap<String, MemoryDef>,
    pub total_memory_size: usize,
    known_constants: Vec<String>,
    known_memories: Vec<String>,
    block_stack: Vec<usize>,
    current_block_id: usize,
}

pub struct Token {
    pub word: lexer::Word,
    pub op: Op,
}

pub enum Op {
    PushInt(u32),
    PushPtr(usize),
    PushBool(bool),
    PushString(String),

    Intrinsic(Intrinsic),
    Const(String),
    ConstRef(String),
    Mem(String),
    MemRef(String),
    Function(String),
    FunctionRef(String),

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
    Store8,
    Store16,
    Store32,
    Load8,
    Load16,
    Load32,
}

impl Display for Op {
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
        let txt = match self {
            Op::PushInt(_) => "PUSH_INT",
            Op::PushPtr(_) => "PUSH_PTR",
            Op::PushBool(_) => "PUSH_BOOL",
            Op::PushString(_) => "PUSH_STRING",

            Op::Intrinsic(_) => "INTRINSIC",
            Op::Const(_) => "CONST",
            Op::ConstRef(_) => "CONST_REF",
            Op::Mem(_) => "CONST",
            Op::MemRef(_) => "CONST_REF",
            Op::Function(_) => "FUNCTION",
            Op::FunctionRef(_) => "FUNCTION_REF",

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
            Intrinsic::Store8 => "STORE_8",
            Intrinsic::Store16 => "STORE_16",
            Intrinsic::Store32 => "STORE_32",
            Intrinsic::Load8 => "LOAD_8",
            Intrinsic::Load16 => "LOAD_16",
            Intrinsic::Load32 => "LOAD_32",
        };
        write!(f, "{}", txt)
    }
}

pub fn parse_words_into_tokens(mut words: Vec<lexer::Word>) -> ParserContext {
    words.reverse();
    let mut ctx = ParserContext {
        result: vec![],
        constants: HashMap::new(),
        functions: HashMap::new(),
        memories: HashMap::new(),
        total_memory_size: 0,
        known_constants: vec![],
        known_memories: vec![],
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
        if "memory" == word.txt {
            if words.is_empty() {
                eprintln!("{}: ERROR: Encountered incomplete memory definition", word);
                std::process::exit(1);
            }
            ctx.block_stack.push(ctx.current_block_id);
            ctx.current_block_id += 1;
            let mem_name = words.pop().unwrap().txt;
            ctx.result.push(Token {
                word,
                op: Op::Mem(mem_name.clone()),
            });
            ctx.known_memories.push(mem_name);
            continue;
        }
        if "function" == word.txt {
            if words.is_empty() {
                eprintln!("{}: ERROR: Encountered incomplete function signature", word);
                std::process::exit(1);
            }
            ctx.block_stack.push(ctx.current_block_id);
            ctx.current_block_id += 1;
            let function_token = parse_function(&mut ctx, &mut words, &word);
            ctx.result.push(function_token);
            continue;
        }
        if ctx.known_constants.contains(&word.txt) {
            let name = word.txt.clone();
            ctx.result.push(Token { word, op: Op::ConstRef(name) });
            continue;
        }
        if ctx.known_memories.contains(&word.txt) {
            let name = word.txt.clone();
            ctx.result.push(Token { word, op: Op::MemRef(name) });
            continue;
        }
        if ctx.functions.contains_key(&word.txt) {
            let name = word.txt.clone();
            ctx.result.push(Token { word, op: Op::FunctionRef(name) });
            continue;
        }
        eprintln!("{}: ERROR: Unknown word: '{}'", word, word.txt);
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
        "store8" | "store" => Some(Intrinsic::Store8),
        "store16" => Some(Intrinsic::Store16),
        "store32" => Some(Intrinsic::Store32),
        "load8" | "load" => Some(Intrinsic::Load8),
        "load16" => Some(Intrinsic::Load16),
        "load32" => Some(Intrinsic::Load32),
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

fn parse_function(ctx: &mut ParserContext, words: &mut Vec<Word>, function_word: &Word) -> Token {
    let mut next_word = words.pop().unwrap();
    if next_word.txt.contains("\"") || next_word.txt.contains("\'") {
        eprintln!("{}: ERROR: Function name cannot contain any quotes", next_word);
        std::process::exit(1);
    }
    let mut parts: Vec<lexer::Word> = vec![];
    let mut buffer: String = String::from("");
    let mut ptr: usize = 0;
    'MainLoop: while !words.is_empty() {
        let txt = next_word.txt.clone();
        let mut i = 0;
        while i < txt.len() {
            let c = txt.chars().nth(i).unwrap();
            if c == '(' || c == ' ' || c == ')' {
                if ptr > 0 {
                    parts.push(lexer::Word {
                        file: next_word.file.clone(),
                        row: next_word.row,
                        col: next_word.col,
                        txt: buffer.clone(),
                    });
                    buffer = String::from("");
                    ptr = 0;
                }
                if c == ')' {
                    break 'MainLoop;
                }
                i += 1;
                continue;
            }
            if c == '-' && i + 1 < txt.len() && txt.chars().nth(i + 1).unwrap() == '>' {
                parts.push(lexer::Word {
                    file: next_word.file.clone(),
                    row: next_word.row,
                    col: next_word.col,
                    txt: String::from("->"),
                });
                i += 2;
                ptr = 0;
            } else {
                buffer += &c.to_string();
                ptr += 1;
            }
            i += 1;
        }
        if (ptr > 0) {
            parts.push(lexer::Word {
                file: next_word.file.clone(),
                row: next_word.row,
                col: next_word.col,
                txt: String::from(buffer),
            });
            buffer = String::from("");
            ptr = 0;
        }
        next_word = words.pop().unwrap();
    }
    let func_name_word = parts.first().unwrap().clone();
    parts.remove(0);
    let mut ins: Vec<TypedPos> = vec![];
    let mut outs: Vec<TypedPos> = vec![];
    let mut input = true;
    for part in parts {
        if part.txt == "->" {
            input = false;
        } else {
            let data_type = checker::get_data_type_by_text(&part.txt);
            match data_type {
                Some(typ) => {
                    if (input) {
                        ins.push(TypedPos { word: part, typ: typ.clone() });
                    } else {
                        outs.push(TypedPos { word: part, typ: typ.clone() });
                    }
                }
                None => {
                    eprintln!(
                        "{}: ERROR: Function signature containts unknown {} type: '{}'",
                        part,
                        if input { "input" } else { "output" },
                        part.txt
                    );
                    std::process::exit(1);
                }
            }
        }
    }
    ctx.functions.insert(func_name_word.txt.clone(), FunctionDef { ins, outs });
    Token {
        word: Word {
            file: function_word.file.clone(),
            row: function_word.row,
            col: function_word.col,
            txt: func_name_word.txt.clone(),
        },
        op: Op::Function(func_name_word.txt.clone()),
    }
}
