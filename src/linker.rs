use crate::linker::LinkedTokenData::JumpAddr;
use crate::tokenizer;
use crate::tokenizer::{Intrinsic, Op};
use crate::{checker, lexer};
use std::fmt::{Display, Formatter};

#[derive(Copy, Clone)]
pub enum LinkedTokenData {
    None,

    JumpAddr(usize),
}

pub enum Instruction {
    PushInt(u32),
    PushPtr(usize),
    PushBool(bool),
    PushString(String),

    Intrinsic(Intrinsic),

    Jump,
    JumpNeq,
    Do,
}

impl Display for Instruction {
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
        let txt = match self {
            Instruction::PushInt(_) => "PUSH_INT",
            Instruction::PushPtr(_) => "PUSH_POINTER",
            Instruction::PushBool(_) => "PUSH_BOOL",
            Instruction::PushString(_) => "PUSH_STRING",

            Instruction::Intrinsic(_) => "INTRINSIC",

            Instruction::Jump => "JUMP",
            Instruction::JumpNeq => "JUMP_NEQ",
            Instruction::Do => "DO",
        };
        write!(f, "{}", txt)
    }
}

pub struct LinkedToken {
    pub word: lexer::Word,
    pub self_ptr: usize,
    pub instruction: Instruction,
    pub data: LinkedTokenData,
}

pub struct LinkerContext {
    tokens: Vec<tokenizer::Token>,
    pub result: Vec<LinkedToken>,
    call_stack: Vec<usize>,
    pub mem_size: usize,
    pointer: usize,
}

impl LinkerContext {
    pub const fn new(tokens: Vec<tokenizer::Token>, mem_size: usize) -> LinkerContext {
        LinkerContext {
            tokens,
            result: vec![],
            call_stack: vec![],
            mem_size,
            pointer: 0,
        }
    }

    fn incr_ptr(&mut self) -> usize {
        let result = self.pointer;
        self.pointer += 1;
        result
    }
}

impl LinkedToken {
    pub fn new_with_data(word: lexer::Word, self_ptr: usize, instruction: Instruction, data: LinkedTokenData) -> LinkedToken {
        LinkedToken {
            word,
            self_ptr,
            instruction,
            data,
        }
    }
    pub fn new(word: lexer::Word, self_ptr: usize, instruction: Instruction) -> LinkedToken {
        LinkedToken::new_with_data(word, self_ptr, instruction, LinkedTokenData::None)
    }
}

pub fn link_tokens(parser_context: tokenizer::ParserContext) -> LinkerContext {
    let mut ctx = LinkerContext::new(parser_context.result, parser_context.total_memory_size);
    ctx.tokens.reverse();
    while !ctx.tokens.is_empty() {
        let token = ctx.tokens.pop().unwrap();
        match &token.op {
            Op::PushInt(val) => {
                let new_token = LinkedToken::new(token.word, ctx.incr_ptr(), Instruction::PushInt(*val));
                ctx.result.push(new_token);
            }
            Op::PushPtr(val) => {
                let new_token = LinkedToken::new(token.word, ctx.incr_ptr(), Instruction::PushPtr(*val));
                ctx.result.push(new_token);
            }
            Op::PushBool(val) => {
                let new_token = LinkedToken::new(token.word, ctx.incr_ptr(), Instruction::PushBool(*val));
                ctx.result.push(new_token);
            }
            Op::PushString(val) => {
                let new_token = LinkedToken::new(token.word, ctx.incr_ptr(), Instruction::PushString(val.clone()));
                ctx.result.push(new_token);
            }
            Op::Intrinsic(val) => {
                let new_token = LinkedToken::new(token.word, ctx.incr_ptr(), Instruction::Intrinsic(*val));
                ctx.result.push(new_token);
            }
            Op::Const(_) => panic!("Constants should have been removed during evaluation"),
            Op::Mem(_) => panic!("memories should have been removed during evaluation"),
            Op::ConstRef(name) => {
                let def = parser_context.constants.get(name).unwrap_or_else(|| {
                    eprintln!("{}: ERROR: Encountered a reference to a nonexistent constant '{}'", token.word, name);
                    std::process::exit(1);
                });
                let new_token = match def.typ {
                    checker::DataType::INT => LinkedToken::new(token.word, ctx.incr_ptr(), Instruction::PushInt(def.val)),
                    _ => panic!("Encountered unimplemented datatype '{}' of constant '{}'. This is a evaluation error.", def.typ, name),
                };
                ctx.result.push(new_token);
            }
            Op::MemRef(name) => {
                let def = parser_context.memories.get(name).unwrap_or_else(|| {
                    eprintln!(
                        "{}: ERROR: Encountered a reference to a nonexistent memory '{}'. This is a evaluation error.",
                        token.word, name
                    );
                    std::process::exit(1);
                });
                let new_token = LinkedToken::new(token.word, ctx.incr_ptr(), Instruction::PushPtr(def.ptr));
                ctx.result.push(new_token);
            }
            Op::End => {
                if ctx.call_stack.is_empty() {
                    eprintln!("{}: ERROR: Encountered dangling 'end' statement", token.word);
                    std::process::exit(1);
                }
                let ref_ptr = ctx.call_stack.pop().unwrap();
                if ref_ptr >= ctx.result.len() {
                    eprintln!("{}: ERROR: Encountered 'end' statement with an invalid reference. This is a linking error.", token.word);
                    std::process::exit(1);
                }
                let ref_token = &mut ctx.result[ref_ptr];
                match &ref_token.instruction {
                    Instruction::Jump | Instruction::JumpNeq => {
                        ref_token.data = JumpAddr(ctx.pointer);
                    }
                    Instruction::Do => {
                        let old_ref_data = ref_token.data;
                        //The DO instruction skips the block if on false values.
                        ref_token.data = JumpAddr(ctx.pointer + 1);
                        let new_token = LinkedToken::new_with_data(token.word, ctx.incr_ptr(), Instruction::Jump, old_ref_data);
                        ctx.result.push(new_token);
                    }
                    _ => {
                        eprintln!(
                            "{}: ERROR: Encountered 'end' that references an invalid instruction '{}'. This is a linking error.",
                            token.word, ref_token.word.txt
                        );
                        std::process::exit(1);
                    }
                }
            }
            Op::If => {
                ctx.call_stack.push(ctx.pointer);
                let new_token = LinkedToken::new(token.word, ctx.incr_ptr(), Instruction::JumpNeq);
                ctx.result.push(new_token);
            }
            Op::Else => {
                if ctx.call_stack.is_empty() {
                    eprintln!("{}: ERROR: Encountered dangling 'else' statement", token.word);
                    std::process::exit(1);
                }
                let ref_ptr = ctx.call_stack.pop().unwrap();
                if ref_ptr >= ctx.result.len() {
                    eprintln!("{}: ERROR: Encountered 'else' statement with an invalid reference. This is a linking error.", token.word);
                    std::process::exit(1);
                }
                let ref_token = &mut ctx.result[ref_ptr];
                ref_token.data = JumpAddr(ctx.pointer + 1);
                ctx.call_stack.push(ctx.pointer);
                let new_token = LinkedToken::new(token.word, ctx.incr_ptr(), Instruction::Jump);
                ctx.result.push(new_token);
            }
            Op::While => {
                //While statement itself does nothing
                ctx.call_stack.push(ctx.pointer);
            }
            Op::Do => {
                if ctx.call_stack.is_empty() {
                    eprintln!("{}: ERROR: Encountered dangling 'do' statement", token.word);
                    std::process::exit(1);
                }
                let ref_ptr = ctx.call_stack.pop().unwrap();
                if ref_ptr >= ctx.result.len() {
                    eprintln!("{}: ERROR: Encountered 'do' statement with an invalid reference. This is a linking error.", token.word);
                    std::process::exit(1);
                }
                ctx.call_stack.push(ctx.pointer);
                let new_token = LinkedToken::new_with_data(token.word, ctx.incr_ptr(), Instruction::Do, JumpAddr(ref_ptr));
                ctx.result.push(new_token);
            }
        }
    }
    ctx
}
