mod checker;
mod lexer;
mod simulator;
mod tokenizer;

fn main() {
    let words = lexer::parse_line_into_words(String::from("<generated>"), 0, "1 2 + dump");
    let tokens = tokenizer::parse_words_into_tokens(words);
    checker::check_types(&tokens, 0);
    simulator::simulate_tokens(tokens);
}
