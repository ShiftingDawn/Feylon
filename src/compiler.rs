use crate::linker;

pub trait Compiler {
    fn write_op(&mut self, ctx: &linker::LinkerContext);
}

pub fn compile(mut compiler: impl Compiler, ctx: &linker::LinkerContext) {
    compiler.write_op(ctx);
}
