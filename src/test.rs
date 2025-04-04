use crate::{add_or_replace_extension, read_file_contents};
use std::path::Path;

struct TestFile {
    exit_code: i32,
    stdout: Vec<String>,
    stderr: Vec<String>,
}

pub fn test_program(self_path: String, mut file_path: String, print: bool, skip_typecheck: bool, compiler: &str) {
    let absolute_file_path = Path::new(file_path.as_str()).canonicalize().unwrap();
    file_path = absolute_file_path.to_str().unwrap().to_string();
    if compiler != "simulate" {
        compile_test_program(self_path.clone(), file_path.clone(), skip_typecheck, compiler);
    }
    let test_file = parse_test_file(&(file_path.clone() + ".txt"));
    if compiler == "simulate" {
        let mut cmd = std::process::Command::new(self_path);
        cmd.arg("simulate").arg(file_path.clone());
        if skip_typecheck {
            cmd.arg("--unsafe");
        }
        validate_tested_program(&mut cmd, test_file, file_path, print);
    } else {
        let extension = if compiler == "asm-win64" { "exe" } else { "" };
        let test_exe_path = add_or_replace_extension(&file_path, extension);
        let mut cmd = std::process::Command::new(test_exe_path);
        validate_tested_program(&mut cmd, test_file, file_path, print);
    }
}

fn compile_test_program(self_path: String, file_path: String, skip_typecheck: bool, compiler: &str) {
    let mut cmd = std::process::Command::new(self_path);
    cmd.arg("compile").arg(format!("--use={}", compiler));
    if skip_typecheck {
        cmd.arg("--unsafe");
    }
    cmd.arg(file_path.clone()).stdout(std::process::Stdio::inherit()).stderr(std::process::Stdio::inherit());
    match cmd.output() {
        Ok(_) => {
            eprintln!("SUCCESS: Successfully compiled test program {}!", file_path);
            eprintln!("SUCCESS: Binary is located at {}!", file_path);
        }
        Err(err) => {
            eprintln!("ERROR: Could not compile test program {}!", file_path);
            eprintln!("ERROR: {}", err);
            std::process::exit(1);
        }
    }
}

fn validate_tested_program(cmd: &mut std::process::Command, test_file: TestFile, file_path: String, print: bool) {
    let output = cmd.output();
    match output {
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
            match test_text_output(test_file.stdout.clone(), output.stdout) {
                Some((ok, strs)) => {
                    if !ok {
                        eprintln!("ERROR: Tested program did not have the same STDOUT as expected");
                        if print {
                            eprintln!("Expected: {:?}", test_file.stdout);
                            eprintln!("Received: {:?}", strs);
                        }
                        std::process::exit(1);
                    }
                }
                None => {
                    eprintln!("ERROR: Could not check tested program stdout");
                    std::process::exit(1);
                }
            }
            match test_text_output(test_file.stderr.clone(), output.stderr) {
                Some((ok, strs)) => {
                    if !ok {
                        eprintln!("ERROR: Tested program did not have the same STDERR as expected");
                        if print {
                            eprintln!("Expected: {:?}", test_file.stderr);
                            eprintln!("Received: {:?}", strs);
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
                stdout: out,
                stderr: err,
            }
        }
        Err(err) => {
            eprintln!("ERROR: Could not load program test {}!", file);
            eprintln!("ERROR: {}", err);
            std::process::exit(1);
        }
    }
}

fn test_text_output(expected: Vec<String>, actual: Vec<u8>) -> Option<(bool, Vec<String>)> {
    if expected.is_empty() && actual.is_empty() {
        return Some((true, vec![]));
    }
    if let Ok(actual_string) = String::from_utf8(actual.clone()) {
        let splitted: Vec<String> = actual_string.lines().map(|x| x.to_string()).collect();
        return Some((&expected == &splitted, splitted));
    }
    None
}

pub fn run_all_tests(self_path: String, file_path: String, print: bool, skip_typecheck: bool, compiler: &str) {
    let path = std::path::Path::new(file_path.as_str());
    if !path.exists() {
        eprintln!("ERROR: Directory does not exist: {}", file_path);
        std::process::exit(1);
    }
    if !path.is_dir() {
        eprintln!("ERROR: Directory is not a directory: {}", file_path);
        std::process::exit(1);
    }
    let paths: Vec<std::path::PathBuf> = std::fs::read_dir(path)
        .expect("ERROR: Could not read directory")
        .filter_map(|entry| {
            entry.ok().and_then(|e| {
                let path = e.path();
                if path.extension().and_then(|ext| ext.to_str()) == Some("fey") {
                    let result = path.canonicalize().unwrap();
                    println!("INFO: Found test: {}", &result.display());
                    Some(result)
                } else {
                    None
                }
            })
        })
        .collect();
    let mut failed: Vec<String> = vec![];
    for test_path in paths {
        let test_path_string = test_path.to_string_lossy().to_string();
        eprintln!("INFO: Running test: {}", test_path_string);
        let mut cmd_builder = std::process::Command::new(self_path.clone());
        cmd_builder.arg("test");
        if print {
            cmd_builder.arg("--print");
        }
        if skip_typecheck {
            cmd_builder.arg("--unsafe");
        }
        cmd_builder.arg(format!("--use={}", compiler));
        let cmd = cmd_builder
            .arg(test_path_string.clone())
            .stdout(std::process::Stdio::piped())
            .stderr(std::process::Stdio::piped())
            .output();
        match cmd {
            Ok(output) => match output.status.code() {
                Some(code) => {
                    if code != 0 {
                        failed.push(test_path_string.to_string());
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
