package com.shiftingdawn.feylon.tests;

import com.shiftingdawn.feylon.lang.CompilerErrors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class IntrinsicTests extends AbstractTestHost {

	@Test
	public void testDup() {
		this.run("1 dup", 2);
		this.assertStack(1);
		this.assertStack(1);
		this.assertStackEmpty();
		this.assertExceptionWithCode(CompilerErrors.MISSING_ARGUMENTS, "dup");
	}

	@Test
	public void testOver() {
		this.run("1 2 over", 3);
		this.assertStack(1);
		this.assertStack(2);
		this.assertStack(1);
		this.assertStackEmpty();
		this.assertExceptionWithCode(CompilerErrors.MISSING_ARGUMENTS, "over");
		this.assertExceptionWithCode(CompilerErrors.MISSING_ARGUMENTS, "1 over");
	}

	@Test
	public void testSwap() {
		this.run("1 2 swap", 2);
		this.assertStack(1);
		this.assertStack(2);
		this.assertStackEmpty();
		this.assertExceptionWithCode(CompilerErrors.MISSING_ARGUMENTS, "swap");
		this.assertExceptionWithCode(CompilerErrors.MISSING_ARGUMENTS, "1 swap");
	}

	@Test
	public void testRot() {
		this.run("1 2 3 rot", 3);
		this.assertStack(1);
		this.assertStack(3);
		this.assertStack(2);
		this.assertStackEmpty();
		this.assertExceptionWithCode(CompilerErrors.MISSING_ARGUMENTS, "rot");
		this.assertExceptionWithCode(CompilerErrors.MISSING_ARGUMENTS, "1 rot");
		this.assertExceptionWithCode(CompilerErrors.MISSING_ARGUMENTS, "1 2 rot");
	}

	@Test
	public void testAddition() {
		this.run("1 2 +", 1);
		this.assertStack(3);
		this.run("1 2 + 3 +", 1);
		this.assertStack(6);
		this.run("1 2 3 + +", 1);
		this.assertStack(6);
		this.run("0 0 +", 1);
		this.assertStack(0);
		this.assertExceptionWithCode(CompilerErrors.MISSING_ARGUMENTS, "+");
		this.assertExceptionWithCode(CompilerErrors.MISSING_ARGUMENTS, "1 +");
	}

	@Test
	public void testAdditionNeg() {
		this.run("1 -2 +", 1);
		this.assertStack(-1);
		this.run("-6 3 +", 1);
		this.assertStack(-3);
		this.run("-6 -3 +", 1);
		this.assertStack(-9);
		this.assertExceptionWithCode(CompilerErrors.MISSING_ARGUMENTS, "+");
		this.assertExceptionWithCode(CompilerErrors.MISSING_ARGUMENTS, "-1 +");
	}

	@Test
	public void testSubtraction() {
		this.run("3 2 -", 1);
		this.assertStack(1);
		this.run("6 3 - 2 -", 1);
		this.assertStack(1);
		this.run("6 3 2 - -", 1);
		this.assertStack(5);
		this.run("0 0 -", 1);
		this.assertStack(0);
		this.assertExceptionWithCode(CompilerErrors.MISSING_ARGUMENTS, "-");
		this.assertExceptionWithCode(CompilerErrors.MISSING_ARGUMENTS, "1 -");
	}

	@Test
	public void testSubtractionNeg() {
		this.run("3 -2 -", 1);
		this.assertStack(5);
		this.run("3 4 -", 1);
		this.assertStack(-1);
		this.run("-10 -6 -", 1);
		this.assertStack(-4);
		this.assertExceptionWithCode(CompilerErrors.MISSING_ARGUMENTS, "-");
		this.assertExceptionWithCode(CompilerErrors.MISSING_ARGUMENTS, "-1 -");
	}

	@Test
	public void testMultiply() {
		this.run("2 3 *", 1);
		this.assertStack(6);
		this.run("2 0 *", 1);
		this.assertStack(0);
		this.run("0 2 *", 1);
		this.assertStack(0);
		this.assertExceptionWithCode(CompilerErrors.MISSING_ARGUMENTS, "*");
		this.assertExceptionWithCode(CompilerErrors.MISSING_ARGUMENTS, "1 *");
	}

	@Test
	public void testMultiplyNeg() {
		this.run("-2 3 *", 1);
		this.assertStack(-6);
		this.run("2 -3 *", 1);
		this.assertStack(-6);
		this.run("-2 0 *", 1);
		this.assertStack(0);
		this.run("2 -0 *", 1);
		this.assertStack(0);
		this.assertExceptionWithCode(CompilerErrors.MISSING_ARGUMENTS, "*");
		this.assertExceptionWithCode(CompilerErrors.MISSING_ARGUMENTS, "-1 *");
	}

	@Test
	public void testDivide() {
		this.run("2 3 /", 1);
		this.assertStack(0);
		this.run("3 2 /", 1);
		this.assertStack(1);
		this.run("3 3 /", 1);
		this.assertStack(1);
		this.run("4 3 /", 1);
		this.assertStack(1);
		this.assertExceptionWithCode(CompilerErrors.MISSING_ARGUMENTS, "/");
		this.assertExceptionWithCode(CompilerErrors.MISSING_ARGUMENTS, "1 /");
	}

	@Test
	public void testDivideNeg() {
		this.run("-2 3 /", 1);
		this.assertStack(0);
		this.run("-3 3 /", 1);
		this.assertStack(-1);
		this.run("-4 3 /", 1);
		this.assertStack(-1);
		this.assertExceptionWithCode(CompilerErrors.MISSING_ARGUMENTS, "/");
		this.assertExceptionWithCode(CompilerErrors.MISSING_ARGUMENTS, "-1 /");
	}

	@Test
	public void testDivideByZero() {
		Assertions.assertThrows(ArithmeticException.class, () -> this.run("1 0 /", 1));
	}

	@Test
	public void testMod() {
		this.run("10 3 %", 1);
		this.assertStack(1);
		this.run("10 4 %", 1);
		this.assertStack(2);
		Assertions.assertThrows(ArithmeticException.class, () -> this.run("1 0 %", 1));
		Assertions.assertDoesNotThrow(() -> this.run("0 1 %", 1));
		this.run("0 1 %", 1);
		this.assertStack(0);
		this.assertExceptionWithCode(CompilerErrors.MISSING_ARGUMENTS, "%");
		this.assertExceptionWithCode(CompilerErrors.MISSING_ARGUMENTS, "1 %");
	}

	@Test
	public void testEquals() {
		this.run("1 1 + 2 =", 1);
		this.assertStack(1);
		this.run("1 2 + 4 =", 1);
		this.assertStack(0);
		this.assertExceptionWithCode(CompilerErrors.MISSING_ARGUMENTS, "=");
		this.assertExceptionWithCode(CompilerErrors.MISSING_ARGUMENTS, "1 =");
	}

	@Test
	public void testNotEquals() {
		this.run("1 2 + 4 !=", 1);
		this.assertStack(1);
		this.run("1 1 + 2 !=", 1);
		this.assertStack(0);
		this.assertExceptionWithCode(CompilerErrors.MISSING_ARGUMENTS, "!=");
		this.assertExceptionWithCode(CompilerErrors.MISSING_ARGUMENTS, "1 !=");
	}

	@Test
	public void testLess() {
		this.run("1 1 + 3 <", 1);
		this.assertStack(1);
		this.run("1 1 + 2 <", 1);
		this.assertStack(0);
		this.assertExceptionWithCode(CompilerErrors.MISSING_ARGUMENTS, "<");
		this.assertExceptionWithCode(CompilerErrors.MISSING_ARGUMENTS, "1 <");
	}

	@Test
	public void testGreater() {
		this.run("1 1 + 1 >", 1);
		this.assertStack(1);
		this.run("1 1 + 2 >", 1);
		this.assertStack(0);
		this.assertExceptionWithCode(CompilerErrors.MISSING_ARGUMENTS, ">");
		this.assertExceptionWithCode(CompilerErrors.MISSING_ARGUMENTS, "1 >");
	}

	@Test
	public void testLessEqual() {
		this.run("1 1 + 3 <=", 1);
		this.assertStack(1);
		this.run("1 1 + 2 <=", 1);
		this.assertStack(1);
		this.run("1 1 + 1 <=", 1);
		this.assertStack(0);
		this.assertExceptionWithCode(CompilerErrors.MISSING_ARGUMENTS, "<=");
		this.assertExceptionWithCode(CompilerErrors.MISSING_ARGUMENTS, "1 <=");
	}

	@Test
	public void testGreaterEqual() {
		this.run("1 1 + 1 >=", 1);
		this.assertStack(1);
		this.run("1 1 + 2 >=", 1);
		this.assertStack(1);
		this.run("1 1 + 3 >=", 1);
		this.assertStack(0);
		this.assertExceptionWithCode(CompilerErrors.MISSING_ARGUMENTS, ">=");
		this.assertExceptionWithCode(CompilerErrors.MISSING_ARGUMENTS, "1 >=");
	}

	@Test
	public void testShiftLeft() {
		this.run("10 20 <<", 1);
		this.assertStack(10 << 20);
		this.assertExceptionWithCode(CompilerErrors.MISSING_ARGUMENTS, "<<");
		this.assertExceptionWithCode(CompilerErrors.MISSING_ARGUMENTS, "1 <<");
	}

	@Test
	public void testShiftRight() {
		this.run("10 20 >>", 1);
		this.assertStack(10 >> 20);
		this.assertExceptionWithCode(CompilerErrors.MISSING_ARGUMENTS, ">>");
		this.assertExceptionWithCode(CompilerErrors.MISSING_ARGUMENTS, "1 >>");
	}

	@Test
	public void testBitAnd() {
		this.run("10 20 &", 1);
		this.assertStack(10 & 20);
		this.assertExceptionWithCode(CompilerErrors.MISSING_ARGUMENTS, "&");
		this.assertExceptionWithCode(CompilerErrors.MISSING_ARGUMENTS, "1 &");
	}

	@Test
	public void testBitOr() {
		this.run("10 20 |", 1);
		this.assertStack(10 | 20);
		this.assertExceptionWithCode(CompilerErrors.MISSING_ARGUMENTS, "|");
		this.assertExceptionWithCode(CompilerErrors.MISSING_ARGUMENTS, "1 |");
	}

	@Test
	public void testBitXor() {
		this.run("10 20 ^", 1);
		this.assertStack(10 ^ 20);
		this.assertExceptionWithCode(CompilerErrors.MISSING_ARGUMENTS, "^");
		this.assertExceptionWithCode(CompilerErrors.MISSING_ARGUMENTS, "1 ^");
	}
}