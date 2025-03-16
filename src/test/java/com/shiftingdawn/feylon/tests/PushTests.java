package com.shiftingdawn.feylon.tests;

import org.junit.jupiter.api.Test;

public class PushTests extends AbstractTestHost {

	@Test
	public void testPushInt() {
		this.run("1");
		this.assertStack(1);
	}

	@Test
	public void testPushInt3() {
		this.run("1 2 3");
		this.assertStack(3);
		this.assertStack(2);
		this.assertStack(1);
	}

	@Test
	public void testPushString() {
		final String str = "ThisIsAString!";
		this.run('"' + str + '"');
		this.assertStack(str.length()); //Length
		this.assertStack(0);      //Pointer
	}

	@Test
	public void testPushString2() {
		final String str = "ThisIsAString!";
		this.run('"' + str + "\"\n\"" + str + '"');
		this.assertStack(str.length());  //Length
		this.assertStack(str.length());  //Pointer (length of the previous string)
		this.assertStack(str.length());  //Length
		this.assertStack(0);       //Pointer
	}
}