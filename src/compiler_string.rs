use crate::{add_or_replace_extension, linker};
use linker::Instruction;
use std::io::Write;

pub fn process_program(file_path: &str, ctx: &linker::LinkerContext) {
    let output_file_path = add_or_replace_extension(file_path, "cfc");
    let mut out_file = std::fs::File::create(&output_file_path).unwrap_or_else(|e| {
        eprintln!("ERROR: Could not open file for compilation: {}", e);
        std::process::exit(1);
    });
    for token in &ctx.result {
        let op_str = stringify_op(&token);
        out_file
            .write_all(format!("[{}]{:32} //{}\n", token.self_ptr, op_str, token.word).as_bytes())
            .unwrap_or_else(|e| {
                eprintln!("ERROR: Could not open file for compilation: {}", e);
                std::process::exit(1);
            });
    }
    out_file.flush().unwrap_or_else(|e| {
        eprintln!("ERROR: Could not open file for compilation: {}", e);
        std::process::exit(1);
    });
    println!("SUCCESS: Written compilation to: {}", output_file_path);
}

pub fn stringify_op(op: &linker::LinkedToken) -> String {
    let base: String = match &op.instruction {
        Instruction::PushInt(val) => format!("PUSH_INT({})", val),
        Instruction::PushString(val) => format!("PUSH_STRING({})", val),
        Instruction::Intrinsic(val) => val.to_string(),

        _ => op.instruction.to_string(),
    };
    let add: String = match &op.data {
        linker::LinkedTokenData::None => String::from(""),
        linker::LinkedTokenData::JumpAddr(addr) => format!("addr={}", addr),
        linker::LinkedTokenData::Count(count) => format!("count={}", count),
        linker::LinkedTokenData::Index(index) => format!("index={}", index),
    };
    if add.is_empty() { base } else { format!("{}({})", base, add) }
}
