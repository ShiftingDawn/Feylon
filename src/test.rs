use crate::read_file_contents;

struct TestFile {
    exit_code: i32,
    stdout: String,
    stderr: String,
}

pub fn test_program(self_path: String, file_path: String) {
    let test_file = parse_test_file(&(file_path.clone() + ".txt"));
    let cmd = std::process::Command::new(self_path)
        .arg("simulate")
        .arg(file_path.clone())
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
            match test_text_output(&test_file.stdout, output.stdout) {
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
            match test_text_output(&test_file.stderr, output.stderr) {
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
            eprintln!("ERROR: Could not test program {}!", file_path);
            eprintln!("ERROR: {}", err);
            std::process::exit(1);
        }
    }
}

fn parse_test_file(file: &str) -> TestFile {
    match read_file_contents(file) {
        Ok(mut contents) => {
            contents.reverse();
            let exit_code: i32 = contents.pop().unwrap_or_else(|| {
				eprintln!("ERROR: Could not load program test {}!", file);
				eprintln!("ERROR: The first line should be an integer representing the expected exit code");
				std::process::exit(1);
			}).parse().unwrap_or_else(|_| {
				eprintln!("ERROR: Could not load program test {}!", file);
				eprintln!("ERROR: The first line should be an integer representing the expected exit code");
				std::process::exit(1);
			});
            let mut out: Vec<String> = vec![];
            let mut err: Vec<String> = vec![];
            let mut is_stdout = true;
            while !contents.is_empty() {
                let token = contents.pop().unwrap();
                if "out:".eq(token.as_str()) {
                    is_stdout = true;
                } else if "err:".eq(token.as_str()) {
                    is_stdout = false;
                } else if is_stdout {
                    out.push(token);
                } else {
                    err.push(token);
                }
            }
            TestFile {
                exit_code,
                stdout: out.join("\n"),
                stderr: err.join("\n"),
            }
        }
        Err(err) => {
            eprintln!("ERROR: Could not load program test {}!", file);
            eprintln!("ERROR: {}", err);
            std::process::exit(1);
        }
    }
}

fn test_text_output(expected: &str, actual: Vec<u8>) -> Option<(bool, String)> {
    if let Ok(actual_string) = String::from_utf8(actual.clone()) {
        return Some((expected == actual_string, actual_string));
    }
    None
}