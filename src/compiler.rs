use crate::{compiler_asm_elf64, compiler_asm_win64, compiler_string, linker};

pub const KNOWN_COMPILERS: [&str; 3] = ["string", "asm-elf64", "asm-win64"];

pub fn compile(id: &str, input_path: &str, ctx: &linker::LinkerContext) {
    match id {
        "string" => compiler_string::process_program(input_path, ctx),
        "asm-elf64" => compiler_asm_elf64::process_program(input_path, ctx),
        "asm-win64" => compiler_asm_win64::process_program(input_path, ctx),
        _ => panic!(),
    }
}
