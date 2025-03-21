package com.shiftingdawn.feylon.lang;

@FunctionalInterface
public interface LexPredicate {

	boolean test(int p, char x);
}
