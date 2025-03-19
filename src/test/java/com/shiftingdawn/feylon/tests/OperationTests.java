package com.shiftingdawn.feylon.tests;

import com.shiftingdawn.feylon.syntax.CompilerErrors;
import org.junit.jupiter.api.Test;

public class OperationTests extends AbstractTestHost {

	@Test
	public void testIf() {
		this.assertExceptionWithCode(CompilerErrors.UNCLOSED_STATEMENT, "1 1 + 2 = if 3");
		this.assertExceptionWithCode(CompilerErrors.MISSING_ARGUMENTS, "if 3 end");
		this.run("1 1 + 2 = if 3 end", 1);
		this.assertStack(3);
		this.run("1 2 + 4 = if 3 end", 1);
		this.assertStackEmpty();
	}

	@Test
	public void testIfElse() {
		this.run("1 1 + 2 = if 3 else 4 end", 1);
		this.assertStack(3);
		this.run("1 2 + 4 = if 3 else 4 end", 1);
		this.assertStack(4);
	}

	@Test
	public void testWhile() {
		this.run("5 while dup 0 > do 1 - end", 1);
		this.assertStack(0);
	}
}