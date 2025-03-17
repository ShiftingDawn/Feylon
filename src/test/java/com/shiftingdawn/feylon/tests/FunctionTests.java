package com.shiftingdawn.feylon.tests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class FunctionTests extends AbstractTestHost {

	@Test
	public void testWithoutIO() {
		final var sysOut = System.out;
		final ByteArrayOutputStream boas = new ByteArrayOutputStream();
		System.setOut(new PrintStream(boas, true));
		this.run("function test \"ThisIsAString!\" 1 1 syscall3 end test");
		System.setOut(sysOut);
		this.assertStackEmpty();
		Assertions.assertEquals("ThisIsAString!" + System.lineSeparator(), boas.toString());
	}

	@Test
	public void testWithInputs() {
		final var sysOut = System.out;
		final ByteArrayOutputStream boas = new ByteArrayOutputStream();
		System.setOut(new PrintStream(boas, true));
		this.run("function test dump end 1 test");
		System.setOut(sysOut);
		this.assertStackEmpty();
		Assertions.assertEquals("1" + System.lineSeparator(), boas.toString());
	}

	@Test
	public void testWithOutputs() {
		this.run("function test 1 end test");
		this.assertStack(1);
	}

	@Test
	public void testWithIO() {
		this.run("function test 2 + end 3 test");
		this.assertStack(5);
	}
}