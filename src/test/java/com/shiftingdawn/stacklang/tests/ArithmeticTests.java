package com.shiftingdawn.stacklang.tests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ArithmeticTests extends AbstractTestHost {

	@Test
	public void testAddition() {
		this.run("1 2 +");
		this.assertStack(3);
		this.run("1 2 + 3 +");
		this.assertStack(6);
		this.run("1 2 3 + +");
		this.assertStack(6);
		this.run("0 0 +");
		this.assertStack(0);
	}

	@Test
	public void testAdditionNeg() {
		this.run("1 -2 +");
		this.assertStack(-1);
		this.run("-6 3 +");
		this.assertStack(-3);
		this.run("-6 -3 +");
		this.assertStack(-9);
	}

	@Test
	public void testSubtraction() {
		this.run("3 2 -");
		this.assertStack(1);
		this.run("6 3 - 2 -");
		this.assertStack(1);
		this.run("6 3 2 - -");
		this.assertStack(5);
		this.run("0 0 -");
		this.assertStack(0);
	}

	@Test
	public void testSubtractionNeg() {
		this.run("3 -2 -");
		this.assertStack(5);
		this.run("3 4 -");
		this.assertStack(-1);
		this.run("-10 -6 -");
		this.assertStack(-4);
	}

	@Test
	public void testMultiply() {
		this.run("2 3 *");
		this.assertStack(6);
		this.run("2 0 *");
		this.assertStack(0);
		this.run("0 2 *");
		this.assertStack(0);
	}

	@Test
	public void testMultiplyNeg() {
		this.run("-2 3 *");
		this.assertStack(-6);
		this.run("2 -3 *");
		this.assertStack(-6);
		this.run("-2 0 *");
		this.assertStack(0);
		this.run("2 -0 *");
		this.assertStack(0);
	}

	@Test
	public void testDivide() {
		this.run("2 3 /");
		this.assertStack(0);
		this.run("3 2 /");
		this.assertStack(1);
		this.run("3 3 /");
		this.assertStack(1);
		this.run("4 3 /");
		this.assertStack(1);
	}

	@Test
	public void testDivideNeg() {
		this.run("-2 3 /");
		this.assertStack(0);
		this.run("-3 3 /");
		this.assertStack(-1);
		this.run("-4 3 /");
		this.assertStack(-1);
	}

	@Test
	public void testDivideByZero() {
		Assertions.assertThrows(ArithmeticException.class, () -> this.run("1 0 /"));
	}
}