use crate::compiler::Compiler;
use crate::linker;
use linker::Instruction;
use std::io::Write;

pub fn new_string_representation_compiler(input_path: &str) -> StringRepresentationCompiler {
    let output_file_path = format!("{}.cfc", input_path);
    StringRepresentationCompiler { file_path: output_file_path }
}

pub struct StringRepresentationCompiler {
    file_path: String,
}
impl Compiler for StringRepresentationCompiler {
    fn write_op(&mut self, ctx: &linker::LinkerContext) {
        let mut out_file = std::fs::File::create(&self.file_path).unwrap_or_else(|e| {
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
        println!("SUCCESS: Written compilation to: {}", self.file_path);
    }
}

fn stringify_op(op: &linker::LinkedToken) -> String {
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
    };
    if add.is_empty() { base } else { format!("{}({})", base, add) }
}
