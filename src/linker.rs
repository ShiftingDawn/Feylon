use crate::lexer;
use crate::linker::LinkedTokenData::JumpAddr;
use crate::tokenizer;
use crate::tokenizer::Op;

#[derive(Copy, Clone)]
pub enum LinkedTokenData {
    None,

    JumpAddr(usize),
}
pub struct LinkedToken {
    pub word: lexer::Word,
    pub self_ptr: usize,
    pub op: Op,
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
    pub const fn new(tokens: Vec<tokenizer::Token>) -> LinkerContext {
        LinkerContext {
            tokens,
            result: vec![],
            call_stack: vec![],
            mem_size: 0,
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
    pub fn new(word: lexer::Word, self_ptr: usize, op: Op) -> LinkedToken {
        LinkedToken {
            word,
            self_ptr,
            op,
            data: LinkedTokenData::None,
        }
    }
    pub fn new_with_data(word: lexer::Word, self_ptr: usize, op: Op, data: LinkedTokenData) -> LinkedToken {
        LinkedToken { word, self_ptr, op, data }
    }
}

pub fn link_tokens(tokens: Vec<tokenizer::Token>) -> LinkerContext {
    let mut ctx = LinkerContext::new(tokens);
    ctx.tokens.reverse();
    while !ctx.tokens.is_empty() {
        let token = ctx.tokens.pop().unwrap();
        match &token.op {
            Op::PushInt(_) => {
                let new_token = LinkedToken::new(token.word, ctx.incr_ptr(), token.op);
                ctx.result.push(new_token);
            }
            Op::PushBool(_) => {
                let new_token = LinkedToken::new(token.word, ctx.incr_ptr(), token.op);
                ctx.result.push(new_token);
            }
            Op::PushString(_) => {
                let new_token = LinkedToken::new(token.word, ctx.incr_ptr(), token.op);
                ctx.result.push(new_token);
            }
            Op::Intrinsic(_) => {
                let new_token = LinkedToken::new(token.word, ctx.incr_ptr(), token.op);
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
                match &ref_token.op {
                    Op::If => {
                        ref_token.data = JumpAddr(ctx.pointer);
                    }
                    Op::Else => {
                        ref_token.data = JumpAddr(ctx.pointer);
                    }
                    Op::Do => {
                        let old_ref_data = ref_token.data;
                        //The DO instruction skips the block if on false values.
                        ref_token.data = JumpAddr(ctx.pointer + 1);
                        let new_token = LinkedToken::new_with_data(token.word, ctx.incr_ptr(), Op::Else, old_ref_data);
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
                let new_token = LinkedToken::new(token.word, ctx.incr_ptr(), token.op);
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
                let new_token = LinkedToken::new(token.word, ctx.incr_ptr(), token.op);
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
                let new_token = LinkedToken::new_with_data(token.word, ctx.incr_ptr(), token.op, JumpAddr(ref_ptr));
                ctx.result.push(new_token);
            }
        }
    }
    ctx
}
