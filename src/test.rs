use crate::read_file_contents;

struct TestFile {
    exit_code: i32,
    stdout: String,
    stderr: String,
}

pub fn test_program(self_path: String, file_path: String, print: bool) {
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
                        eprintln!("ERROR: Tested program exited with code {} while expected code is {}", code, test_file.exit_code);
                        if print {
                            eprintln!("ERROR: STDERR: {}", String::from_utf8(output.stderr).unwrap());
                            eprintln!("ERROR: STDOUT: {}", String::from_utf8(output.stdout).unwrap());
                        }
                        std::process::exit(1);
                    }
                }
                None => {
                    eprintln!("ERROR: Tested program exited unexpectedly");
                    if print {
                        eprintln!("ERROR: STDERR: {}", String::from_utf8(output.stderr).unwrap());
                        eprintln!("ERROR: STDOUT: {}", String::from_utf8(output.stdout).unwrap());
                    }
                    std::process::exit(1);
                }
            }
            match test_text_output(&test_file.stdout, output.stdout) {
                Some((ok, str)) => {
                    if !ok {
                        eprintln!("ERROR: Tested program did not have the same STDOUT as expected");
                        if print {
                            eprintln!("Expected: {}", test_file.stdout);
                            eprintln!("Received: {}", str);
                        }
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
                        eprintln!("ERROR: Tested program did not have the same STDERR as expected");
                        if print {
                            eprintln!("Expected: {}", test_file.stdout);
                            eprintln!("Received: {}", str);
                        }
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
    match read_file_contents(file, None) {
        Ok(mut contents) => {
            contents.reverse();
            let exit_code: i32 = contents
                .pop()
                .unwrap_or_else(|| {
                    eprintln!("ERROR: Could not load program test {}!", file);
                    eprintln!("ERROR: The first line should be an integer representing the expected exit code");
                    std::process::exit(1);
                })
                .parse()
                .unwrap_or_else(|_| {
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

pub fn run_all_tests(self_path: String, file_path: String, print: bool) {
    let path = std::path::Path::new(file_path.as_str());
    if !path.exists() {
        eprintln!("ERROR: Directory does not exist: {}", file_path);
        std::process::exit(1);
    }
    if !path.is_dir() {
        eprintln!("ERROR: Directory is not a directory: {}", file_path);
        std::process::exit(1);
    }
    match path.read_dir() {
        Ok(dir_it) => {
            let mut found_files: Vec<String> = vec![];
            for dir_entry in dir_it {
                match dir_entry {
                    Ok(entry) => {
                        let name_string = entry.file_name().to_str().unwrap().to_string();
                        let full_path = entry.path().to_str().unwrap().to_string();
                        if name_string.ends_with(".fey") {
                            println!("INFO: Found test: {}", full_path);
                            found_files.push(full_path);
                        } else if !name_string.ends_with(".fey.txt") && !name_string.ends_with(".fey.cfc") {
                            println!("WARN: Not a test: {}", full_path);
                        }
                    }
                    Err(err) => {
                        eprintln!("ERROR: Could not enumerate directory: {}", file_path);
                        eprintln!("{}", err);
                        std::process::exit(1);
                    }
                }
            }
            let mut failed: Vec<String> = vec![];
            for found_file in found_files {
                eprintln!("INFO: Running test: {}", found_file);
                let mut cmd_builder = std::process::Command::new(self_path.clone());
                cmd_builder.arg("test");
                if print {
                    cmd_builder.arg("--print");
                }
                let cmd = cmd_builder
                    .arg(found_file.clone())
                    .stdout(std::process::Stdio::piped())
                    .stderr(std::process::Stdio::piped())
                    .output();
                match cmd {
                    Ok(output) => match output.status.code() {
                        Some(code) => {
                            if code != 0 {
                                failed.push(found_file.to_string());
                                if print {
                                    println!("======== STDOUT ========");
                                    println!("{}", String::from_utf8(output.stdout).unwrap());
                                    println!("======== STDERR ========");
                                    println!("{}", String::from_utf8(output.stderr).unwrap());
                                    println!("========= DONE =========");
                                }
                            }
                        }
                        None => {
                            eprintln!("ERROR: Could not test program {}!", file_path);
                            std::process::exit(1);
                        }
                    },
                    Err(err) => {
                        eprintln!("ERROR: Could not test program {}!", file_path);
                        eprintln!("ERROR: {}", err);
                        std::process::exit(1);
                    }
                }
            }
            if !failed.is_empty() {
                eprintln!("ERROR: Some tests failed:");
                for failed_path in failed {
                    eprintln!("{}", failed_path);
                }
                if !print {
                    eprintln!("Add the --print option to print the test outputs");
                }
                std::process::exit(1);
            }
        }
        Err(err) => {
            eprintln!("ERROR: Could not enumerate directory: {}", file_path);
            eprintln!("{}", err);
            std::process::exit(1);
        }
    }
}
