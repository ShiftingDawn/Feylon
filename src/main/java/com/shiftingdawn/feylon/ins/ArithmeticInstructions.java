package com.shiftingdawn.feylon.ins;

import com.shiftingdawn.feylon.Memory;
import com.shiftingdawn.feylon.Stack;

import java.util.function.IntConsumer;

public class ArithmeticInstructions {

	public static void add(final IntConsumer jump, final Stack data, final Stack returnStack, final Memory memory) {
		data.push(data.pop() + data.pop());
	}

	public static void subtract(final IntConsumer jump, final Stack data, final Stack returnStack, final Memory memory) {
		final int a = data.pop();
		final int b = data.pop();
		data.push(b - a);
	}

	public static void multiply(final IntConsumer jump, final Stack data, final Stack returnStack, final Memory memory) {
		data.push(data.pop() * data.pop());
	}

	public static void divide(final IntConsumer jump, final Stack data, final Stack returnStack, final Memory memory) {
		final int a = data.pop();
		final int b = data.pop();
		data.push(b / a);
	}

	public static void mod(final IntConsumer jump, final Stack data, final Stack returnStack, final Memory memory) {
		final int a = data.pop();
		final int b = data.pop();
		data.push(b % a);
	}

	public static void bitShiftLeft(final IntConsumer jump, final Stack data, final Stack returnStack, final Memory memory) {
		final int amount = data.pop();
		final int x = data.pop();
		data.push(x << amount);
	}

	public static void bitShiftRight(final IntConsumer jump, final Stack data, final Stack returnStack, final Memory memory) {
		final int amount = data.pop();
		final int x = data.pop();
		data.push(x >> amount);
	}

	public static void bitwiseAnd(final IntConsumer jump, final Stack data, final Stack returnStack, final Memory memory) {
		data.push(data.pop() & data.pop());
	}

	public static void bitwiseOr(final IntConsumer jump, final Stack data, final Stack returnStack, final Memory memory) {
		data.push(data.pop() | data.pop());
	}

	public static void bitwiseXor(final IntConsumer jump, final Stack data, final Stack returnStack, final Memory memory) {
		data.push(data.pop() ^ data.pop());
	}

	public static void equals(final IntConsumer jump, final Stack data, final Stack returnStack, final Memory memory) {
		final double a = data.pop();
		final double b = data.pop();
		data.push(a == b);
	}

	public static void notEquals(final IntConsumer jump, final Stack data, final Stack returnStack, final Memory memory) {
		final double a = data.pop();
		final double b = data.pop();
		data.push(a != b);
	}

	public static void less(final IntConsumer jump, final Stack data, final Stack returnStack, final Memory memory) {
		final double a = data.pop();
		final double b = data.pop();
		data.push(b < a);
	}

	public static void greater(final IntConsumer jump, final Stack data, final Stack returnStack, final Memory memory) {
		final double a = data.pop();
		final double b = data.pop();
		data.push(b > a);
	}

	public static void lessEqual(final IntConsumer jump, final Stack data, final Stack returnStack, final Memory memory) {
		final double a = data.pop();
		final double b = data.pop();
		data.push(b <= a);
	}

	public static void greaterEqual(final IntConsumer jump, final Stack data, final Stack returnStack, final Memory memory) {
		final double a = data.pop();
		final double b = data.pop();
		data.push(b >= a);
	}
}