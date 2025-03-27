use crate::linker;
use std::io::Write;
use crate::tokenizer;

pub fn write_parsed_program_to_file(input_path: &str, ctx: &linker::LinkerContext) {
	let output_file_path = format!("{}.cfc", input_path);
	let mut out_file = std::fs::File::create(&output_file_path).unwrap_or_else(|e| {
		eprintln!("ERROR: Could not open file for bytecode representation: {}", e);
		std::process::exit(1);
	});
	for token in &ctx.result {
		let op_str = stringify_op(&token.op);
		out_file.write_all(format!("{:32} //{}\n", op_str, token.word).as_bytes()).unwrap_or_else(|e| {
			eprintln!("ERROR: Could not open file for bytecode representation: {}", e);
			std::process::exit(1);
		});
	}
	out_file.flush().unwrap_or_else(|e| {
		eprintln!("ERROR: Could not open file for bytecode representation: {}", e);
		std::process::exit(1);
	});
	eprintln!("SUCCESS: Written bytecode representation to: {}", output_file_path);
}

fn stringify_op(op: &tokenizer::Op) -> String {
	match op {
		tokenizer::Op::PushInt(val) => format!("PUSH_INT({})", val),
		tokenizer::Op::PushString(val) => format!("PUSH_STRING({})", val),
		tokenizer::Op::Intrinsic(val) => val.to_string(),
		tokenizer::Op::Keyword(val) => val.to_string(),
	}
}