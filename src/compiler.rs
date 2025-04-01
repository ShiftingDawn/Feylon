use crate::{compiler_fasm, compiler_string, linker};

pub const KNOWN_COMPILERS: [&str; 2] = ["string", "fasm"];

pub fn compile(id: &str, input_path: &str, ctx: &linker::LinkerContext) {
    match id {
        "string" => compiler_string::process_program(input_path, ctx),
        "fasm" => compiler_fasm::process_program(input_path, ctx),
        _ => panic!(),
    }
}
