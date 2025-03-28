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

pub fn parse_lines_into_words(file: String, source_lines: Vec<String>) -> Vec<Word> {
    let mut lines: Vec<&str> = source_lines.iter().map(|x| x.as_str()).collect();
    lines.reverse();
    let mut result: Vec<Word> = vec![];
    let mut row: i64 = -1;
    while !lines.is_empty() {
        let mut line = lines.pop().unwrap();
        row += 1;
        let mut pos = find_char(&line, 0, |x| !x.is_whitespace());
        while pos < line.len() {
            if line.chars().nth(pos).unwrap() == '\'' {
                let end_pos = find_char(&line, pos + 2, |x| x == '\'');
                if end_pos >= line.len() || (line.chars().nth(pos + 1).unwrap() != '\\' && end_pos - pos >= 3) || (line.chars().nth(pos + 1).unwrap() == '\\' && end_pos - pos >= 4)
                {
                    eprintln!("{}:{}:{}: ERROR: Encountered invalid character literal", file, row + 1, pos + 1);
                    std::process::exit(1);
                }
                let val = line[pos..end_pos].chars().nth(0).unwrap();
                result.push(Word {
                    file: file.clone(),
                    row: row as u32,
                    col: pos as u32,
                    txt: (val as u32).to_string(),
                });
                pos = find_char(&line, end_pos + 1, |x| !x.is_whitespace());
                continue;
            } else if line.chars().nth(pos).unwrap() == '"' {
                let end_pos = find_char(&line, pos + 1, |x| x == '"');
                if end_pos >= line.len() {
                    let start_row = row;
                    let start_col = pos;
                    let mut string_buffer = vec![&line[pos..]];
                    pos = 0;
                    while !lines.is_empty() {
                        line = lines.pop().unwrap();
                        row += 1;
                        let end_pos = find_char(&line, pos + 1, |x| x == '"');
                        if end_pos >= line.len() {
                            string_buffer.push(line);
                        } else {
                            string_buffer.push(&line[0..(end_pos + 1)]);
                            result.push(Word {
                                file: file.clone(),
                                row: start_row as u32,
                                col: start_col as u32,
                                txt: string_buffer.join("\n"),
                            });
                            pos = find_char(&line, end_pos + 2, |x| !x.is_whitespace());
                            break;
                        }
                    }
                } else {
                    result.push(Word {
                        file: file.clone(),
                        row: row as u32,
                        col: pos as u32,
                        txt: line[pos..end_pos + 1].to_string(),
                    });
                    pos = find_char(&line, end_pos + 2, |x| !x.is_whitespace());
                    continue;
                }
            }
            let splitted: Vec<&str> = line.split("//").collect();
            if splitted.is_empty() {
                break;
            }
            line = splitted.first().unwrap();
            let end_pos = find_char(&line, pos + 1, |x| x.is_whitespace());
            if end_pos > line.len() {
                break;
            }
            let token_text = line[pos..end_pos].to_string();
            if token_text.starts_with("//") {
                break;
            }
            result.push(Word {
                file: file.clone(),
                row: row as u32,
                col: pos as u32,
                txt: token_text,
            });
            pos = find_char(&line, end_pos + 1, |x| !x.is_whitespace());
        }
    }
    result
}
