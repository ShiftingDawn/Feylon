package com.shiftingdawn.stacklang.tests;

import org.junit.jupiter.api.Test;

public class ConditionTests extends AbstractTestHost {

	@Test
	public void testEqualsTrue() {
		this.run("1 1 + 2 =");
		this.assertStack(1);
	}

	@Test
	public void testEqualsFalse() {
		this.run("1 2 + 4 =");
		this.assertStack(0);
	}

	@Test
	public void testIfTrue() {
		this.run("1 1 + 2 = if 3 end");
		this.assertStack(3);
	}

	@Test
	public void testIfFalse() {
		this.run("1 2 + 4 = if 3 end");
		this.assertStackEmpty();
	}
}