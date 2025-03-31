use crate::linker::LinkerContext;
use std::fs::File;
use std::io::{self, BufRead, BufReader};
use std::path::Path;

mod bytewriter;
mod checker;
mod evaluator;
mod lexer;
mod linker;
mod simulator;
mod test;
mod tokenizer;

fn main() {
    let mut args: Vec<String> = std::env::args().collect();
    let self_path = args.remove(0);
    if args.len() == 0 {
        usage(&self_path);
        std::process::exit(0);
    }

    let command = match args[0].as_str() {
        "compile" => "compile",
        "test" => "test",
        _ => "simulate",
    };
    let last_arg = args.pop().unwrap();
    match command {
        "simulate" => {
            let skip_typecheck = args.contains(&"--unsafe".to_string());
            simulate_program(&last_arg, skip_typecheck)
        }
        "compile" => match read_file_contents(&last_arg, None) {
            Ok(lines) => {
                let skip_typecheck = args.contains(&"--unsafe".to_string());
                let program = compile_program(last_arg.clone(), lines, skip_typecheck);
                bytewriter::write_parsed_program_to_file(&last_arg, &program);
                std::process::exit(0);
            }
            Err(err) => {
                eprintln!("ERROR: Could not load program {}!", last_arg);
                eprintln!("ERROR: {}", err);
            }
        },
        "test" => {
            let print_output = args.contains(&"--print".to_string());
            if args.contains(&"--all".to_string()) {
                test::run_all_tests(self_path, last_arg, print_output)
            } else {
                test::test_program(self_path, last_arg, print_output);
            }
            std::process::exit(0);
        }
        _ => {
            usage(&self_path);
            std::process::exit(0);
        }
    };
}

fn simulate_program(path: &String, skip_typecheck: bool) {
    match read_file_contents(&path, None) {
        Ok(lines) => {
            let program = compile_program(path.clone(), lines, skip_typecheck);
            simulator::simulate_tokens(program);
            std::process::exit(0);
        }
        Err(err) => {
            eprintln!("ERROR: Could not load program {}!", path);
            eprintln!("ERROR: {}", err);
        }
    }
    std::process::exit(0);
}

fn usage(self_path: &str) {
    println!("Usage: {} <COMMAND> [OPTIONS] <file_path>", self_path);
    println!("Available commands:");
    println!("  simulate        Interpret and simulate the given program");
    println!("    Available options:");
    println!("  compile         Compile the given program and write it to disk");
    println!("    Available options:");
    println!("      --unsafe    Skip typechecking");
    println!("  test            Interpret and test the given program");
    println!("    Available options:");
    println!("      --all       Run all tests in the given directory; file_path must be a directory");
    println!("      --print     Print the program STDOUT and STDERR");
}

pub fn read_file_contents(path: &str, relative_parent: Option<&str>) -> io::Result<Vec<String>> {
    let file_path = match relative_parent {
        Some(parent) => Path::new(parent).parent().unwrap_or_else(|| Path::new("")).join(path),
        None => Path::new(path).to_path_buf(),
    };
    let file = File::open(file_path)?;
    let reader = BufReader::new(file);
    let mut lines = Vec::new();
    for line in reader.lines() {
        let line = line?;
        lines.push(line);
    }
    Ok(lines)
}

fn compile_program(file: String, lines: Vec<String>, skip_typecheck: bool) -> LinkerContext {
    let words = lexer::parse_lines_into_words(file, lines);
    let mut tokens = tokenizer::parse_words_into_tokens(words);
    evaluator::evaluate_tokens(&mut tokens);
    let linked = linker::link_tokens(tokens);
    if !skip_typecheck {
        checker::check_types(&linked, 0);
    }
    linked
}
