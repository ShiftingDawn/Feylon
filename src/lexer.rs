use std::fmt::{Display, Formatter};

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

pub struct Word {
    pub file: String,
    pub row: u32,
    pub col: u32,
    pub txt: String,
}

impl Display for Word {
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
        write!(f, "{}:{}:{}", self.file, self.row + 1, self.col + 1)
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

struct LexerContext<'a> {
    file: String,
    lines: Vec<&'a str>,
    pos: usize,
    row: usize,
    result: Vec<Word>,
    line: &'a str,
}

pub fn parse_lines_into_words(file: String, source_lines: Vec<String>) -> Vec<Word> {
    let mut ctx = LexerContext {
        file,
        lines: source_lines.iter().map(|x| x.as_str()).collect(),
        pos: 0,
        row: 0,
        result: vec![],
        line: "",
    };
    ctx.lines.reverse();
    while !ctx.lines.is_empty() {
        ctx.line = ctx.lines.pop().unwrap();
        ctx.row += 1;
        ctx.pos = find_char(&ctx.line, 0, |x| !x.is_whitespace());
        while ctx.pos < ctx.line.len() {
            if ctx.line.chars().nth(ctx.pos).unwrap() == '\'' {
                parse_char(&mut ctx);
                continue;
            } else if ctx.line.chars().nth(ctx.pos).unwrap() == '"' {
                parse_string(&mut ctx);
                continue;
            }
            let splitted: Vec<&str> = ctx.line.split("//").collect();
            if splitted.is_empty() {
                break;
            }
            ctx.line = splitted.first().unwrap();
            let end_pos = find_char(&ctx.line, ctx.pos + 1, |x| x.is_whitespace());
            if end_pos > ctx.line.len() {
                break;
            }
            let token_text = ctx.line[ctx.pos..end_pos].to_string();
            if token_text.starts_with("//") {
                break;
            }
            ctx.result.push(Word {
                file: ctx.file.clone(),
                row: ctx.row as u32,
                col: ctx.pos as u32,
                txt: token_text,
            });
            ctx.pos = find_char(&ctx.line, end_pos + 1, |x| !x.is_whitespace());
        }
    }
    ctx.result
}

fn parse_char(ctx: &mut LexerContext) {
    let end_pos = find_char(&ctx.line, ctx.pos + 2, |x| x == '\'');
    if end_pos >= ctx.line.len()
        || (ctx.line.chars().nth(ctx.pos + 1).unwrap() != '\\' && end_pos - ctx.pos >= 3)
        || (ctx.line.chars().nth(ctx.pos + 1).unwrap() == '\\' && end_pos - ctx.pos >= 4)
    {
        eprintln!("{}:{}:{}: ERROR: Encountered invalid character literal", ctx.file, ctx.row + 1, ctx.pos + 1);
        std::process::exit(1);
    }
    let val = ctx.line[ctx.pos..end_pos].chars().nth(0).unwrap();
    ctx.result.push(Word {
        file: ctx.file.clone(),
        row: ctx.row as u32,
        col: ctx.pos as u32,
        txt: (val as u32).to_string(),
    });
    ctx.pos = find_char(&ctx.line, end_pos + 1, |x| !x.is_whitespace());
}

fn parse_string(ctx: &mut LexerContext) {
    let end_pos = find_char(&ctx.line, ctx.pos + 1, |x| x == '"');
    if end_pos >= ctx.line.len() {
        let start_row = ctx.row;
        let start_col = ctx.pos;
        let mut string_buffer = vec![&ctx.line[ctx.pos..]];
        ctx.pos = 0;
        while !ctx.lines.is_empty() {
            ctx.line = ctx.lines.pop().unwrap();
            ctx.row += 1;
            let end_pos = find_char(&ctx.line, ctx.pos + 1, |x| x == '"');
            if end_pos >= ctx.line.len() {
                string_buffer.push(ctx.line);
            } else {
                string_buffer.push(&ctx.line[0..(end_pos + 1)]);
                ctx.result.push(Word {
                    file: ctx.file.clone(),
                    row: start_row as u32,
                    col: start_col as u32,
                    txt: string_buffer.join("\n"),
                });
                ctx.pos = find_char(&ctx.line, end_pos + 2, |x| !x.is_whitespace());
                break;
            }
        }
    } else {
        ctx.result.push(Word {
            file: ctx.file.clone(),
            row: ctx.row as u32,
            col: ctx.pos as u32,
            txt: ctx.line[ctx.pos..end_pos + 1].to_string(),
        });
        ctx.pos = find_char(&ctx.line, end_pos + 2, |x| !x.is_whitespace());
    }
}
