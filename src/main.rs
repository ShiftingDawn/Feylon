use crate::linker::LinkerContext;
use std::fs::File;
use std::io::{self, BufRead, BufReader};

mod checker;
mod lexer;
mod simulator;
mod test;
mod tokenizer;
mod linker;

fn main() {
    let mut args: Vec<String> = std::env::args().collect();
    if args.len() < 3 {
        usage(&args[0]);
        std::process::exit(0);
    }

    let last_arg = args.pop().unwrap();
    match args[1].as_str() {
        "simulate" => match read_file_contents(&last_arg) {
            Ok(lines) => {
                let program = compile_program(last_arg.clone(), lines);
                simulator::simulate_tokens(program);
                std::process::exit(0);
            }
            Err(err) => {
                eprintln!("ERROR: Could not load program {}!", last_arg);
                eprintln!("ERROR: {}", err);
            }
        },
        "test" => {
            if "--built-in" == last_arg {
                run_builtin_test();
                std::process::exit(0);
            } else {
                test::test_program(args[0].clone(), last_arg);
                std::process::exit(0);
            }
        }
        _ => {
            usage(&args[0]);
            std::process::exit(0);
        }
    };
}

fn usage(self_path: &str) {
    println!("Usage: {} <COMMAND> [OPTIONS] <file_path>", self_path);
    println!("Available commands:");
    println!("  simulate        Interpret and simulate the given program");
    println!("    Available options:");
    println!("  test            Interpret and test the given program");
    println!("    Available options:");
    println!("      --built-in  Run the built-in test program. Does not need a file path.");
}

pub fn read_file_contents(path: &str) -> io::Result<Vec<String>> {
    let file = File::open(path)?;
    let reader = BufReader::new(file);
    let mut lines = Vec::new();
    for line in reader.lines() {
        let line = line?;
        lines.push(line);
    }
    Ok(lines)
}

fn compile_program(file: String, lines: Vec<String>) -> LinkerContext {
    let words = lexer::parse_lines_into_words(file, lines);
    let tokens = tokenizer::parse_words_into_tokens(words);
    let linked = linker::link_tokens(tokens);
    checker::check_types(&linked, 0);
    linked
}

fn run_builtin_test() {
    let tokens = compile_program(
        String::from("<generated>"),
        vec![String::from("1 2 + dump")],
    );
    checker::check_types(&tokens, 0);
    simulator::simulate_tokens(tokens);
}
