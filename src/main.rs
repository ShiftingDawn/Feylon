use crate::tokenizer::Token;
use std::fs::File;
use std::io::{self, BufRead, BufReader};

mod checker;
mod lexer;
mod simulator;
mod test;
mod tokenizer;

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
                let program = compile_program(last_arg, lines);
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
                let test_file = test::parse_test_file(&(last_arg.clone() + ".txt"));
                let cmd = std::process::Command::new(&args[0])
                    .arg("simulate")
                    .arg(&last_arg)
                    .stdout(std::process::Stdio::piped())
                    .stderr(std::process::Stdio::piped())
                    .output();
                match cmd {
                    Ok(output) => {
                        match output.status.code() {
                            Some(code) => {
                                if test_file.exit_code != code {
                                    eprintln!(
                                        "ERROR: Tested program exited with code {} while expected code is {}",
                                        code, test_file.exit_code
                                    );
                                    eprintln!("ERROR: STDERR: {}", String::from_utf8(output.stderr).unwrap());
                                    eprintln!("ERROR: STDOUT: {}", String::from_utf8(output.stdout).unwrap());
                                    std::process::exit(1);
                                }
                            }
                            None => {
                                eprintln!("ERROR: Tested program exited unexpectedly");
                                eprintln!("ERROR: STDERR: {}", String::from_utf8(output.stderr).unwrap());
                                eprintln!("ERROR: STDOUT: {}", String::from_utf8(output.stdout).unwrap());
                                std::process::exit(1);
                            }
                        }
                        match test::test_text_output(&test_file.stdout, output.stdout) {
                            Some((ok, str)) => {
                                if !ok {
                                    eprintln!(
                                        "ERROR: Tested program did not have the same STDOUT as expected"
                                    );
                                    eprintln!("Expected: {}", test_file.stdout);
                                    eprintln!("Received: {}", str);
                                    std::process::exit(1);
                                }
                            }
                            None => {
                                eprintln!("ERROR: Could not check tested program stdout");
                                std::process::exit(1);
                            }
                        }
                        match test::test_text_output(&test_file.stderr, output.stderr) {
                            Some((ok, str)) => {
                                if !ok {
                                    eprintln!(
                                        "ERROR: Tested program did not have the same STDERR as expected"
                                    );
                                    eprintln!("Expected: {}", test_file.stdout);
                                    eprintln!("Received: {}", str);
                                    std::process::exit(1);
                                }
                            }
                            None => {
                                eprintln!("ERROR: Could not check tested program stderr");
                                std::process::exit(1);
                            }
                        }
                        println!("SUCCESS: Tested program passed");
                        std::process::exit(0);
                    }
                    Err(err) => {
                        eprintln!("ERROR: Could not test program {}!", last_arg);
                        eprintln!("ERROR: {}", err);
                        std::process::exit(1);
                    }
                }
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

fn compile_program(file: String, lines: Vec<String>) -> Vec<Token> {
    let words = lexer::parse_lines_into_words(file, lines);
    let tokens = tokenizer::parse_words_into_tokens(words);
    checker::check_types(&tokens, 0);
    tokens
}

fn run_builtin_test() {
    let tokens = compile_program(
        String::from("<generated>"),
        vec![String::from("1 2 + dump")],
    );
    checker::check_types(&tokens, 0);
    simulator::simulate_tokens(tokens);
}
