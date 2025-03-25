fn main() {
    println!("{:?}", parse_line_into_words("Hello, world!"));
}

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

fn parse_line_into_words(line: &str) -> Vec<String> {
    let mut result = vec![];
    let mut pos = find_char(line, 0, |x| !x.is_whitespace());
    while pos < line.len() {
        let end_pos = find_char(&line, pos + 1, |x| x.is_whitespace());
        result.push(line[pos..end_pos].to_string());
        pos = find_char(line, end_pos + 1, |x| !x.is_whitespace());
    }
    result
}
