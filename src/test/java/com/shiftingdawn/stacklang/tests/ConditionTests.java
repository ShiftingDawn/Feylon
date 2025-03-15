package com.shiftingdawn.stacklang.tests;

import org.junit.jupiter.api.Test;

public class ConditionTests extends AbstractTestHost {

	@Test
	public void testEquals() {
		this.run("1 1 + 2 =");
		this.assertStack(1);
		this.run("1 2 + 4 =");
		this.assertStack(0);
	}

	@Test
	public void testNotEquals() {
		this.run("1 2 + 4 !=");
		this.assertStack(1);
		this.run("1 1 + 2 !=");
		this.assertStack(0);
	}

	@Test
	public void testLess() {
		this.run("1 1 + 3 <");
		this.assertStack(1);
		this.run("1 1 + 2 <");
		this.assertStack(0);
	}

	@Test
	public void testGreater() {
		this.run("1 1 + 1 >");
		this.assertStack(1);
		this.run("1 1 + 2 >");
		this.assertStack(0);
	}

	@Test
	public void testLessEqual() {
		this.run("1 1 + 3 <=");
		this.assertStack(1);
		this.run("1 1 + 2 <=");
		this.assertStack(1);
		this.run("1 1 + 1 <=");
		this.assertStack(0);
	}

	@Test
	public void testGreaterEqual() {
		this.run("1 1 + 1 >=");
		this.assertStack(1);
		this.run("1 1 + 2 >=");
		this.assertStack(1);
		this.run("1 1 + 3 >=");
		this.assertStack(0);
	}

	@Test
	public void testIf() {
		this.run("1 1 + 2 = if 3 end");
		this.assertStack(3);
		this.run("1 2 + 4 = if 3 end");
		this.assertStackEmpty();
	}

	@Test
	public void testIfElse() {
		this.run("1 1 + 2 = if 3 else 4 end");
		this.assertStack(3);
		this.run("1 2 + 4 = if 3 else 4 end");
		this.assertStack(4);
	}

	@Test
	public void testWhile() {
		this.run("5 while dup 0 > do 1 - end");
		this.assertStack(0);
	}
}