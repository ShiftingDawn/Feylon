package com.shiftingdawn.feylon.tests;

import com.shiftingdawn.feylon.syntax.CompilerErrors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FunctionTests extends AbstractTestHost {

	@Test
	public void testWithoutIO() {
		final var sysOut = System.out;
		final ByteArrayOutputStream boas = new ByteArrayOutputStream();
		System.setOut(new PrintStream(boas, true));
		this.run("function test() \"ThisIsAString!\" 1 1 syscall3 end test", 0);
		System.setOut(sysOut);
		this.assertStackEmpty();
		assertEquals("ThisIsAString!" + System.lineSeparator(), boas.toString());
	}

	@Test
	public void testWithInputs() {
		final var sysOut = System.out;
		final ByteArrayOutputStream boas = new ByteArrayOutputStream();
		System.setOut(new PrintStream(boas, true));
		this.run("function test(int) dump end 1 test", 0);
		System.setOut(sysOut);
		this.assertStackEmpty();
		assertEquals("1" + System.lineSeparator(), boas.toString());
	}

	@Test
	public void testWithOutputs() {
		this.run("function test(-> int) 1 end test", 1);
		this.assertStack(1);
	}

	@Test
	public void testWithIO() {
		this.run("function test(int int -> int) + end 3 2 test", 1);
		this.assertStack(5);
	}

	@Test
	public void testInvalidSignatures() {
		Assertions.assertDoesNotThrow(() -> "function test() 1 print end");
		this.assertExceptionWithCode(CompilerErrors.INVALID_FUNCTION_SIGNATURE, "function test 1 print end");
		this.assertExceptionWithCode(CompilerErrors.INVALID_FUNCTION_SIGNATURE, "function test( 1 print end");
		this.assertExceptionWithCode(CompilerErrors.INVALID_FUNCTION_SIGNATURE, "function test( 1 print end");
	}
}