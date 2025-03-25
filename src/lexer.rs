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

#[derive(Debug)]
pub struct Word {
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

pub fn parse_line_into_words(file: String, row: u32, line: &str) -> Vec<Word> {
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