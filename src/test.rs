use crate::read_file_contents;

pub struct TestFile {
    pub exit_code: i32,
    pub stdout: String,
    pub stderr: String,
}

pub fn parse_test_file(file: &str) -> TestFile {
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

pub fn test_text_output(expected: &str, actual: Vec<u8>) -> Option<(bool, String)> {
    if let Ok(actual_string) = String::from_utf8(actual.clone()) {
        return Some((expected == actual_string, actual_string));
    }
    None
}
