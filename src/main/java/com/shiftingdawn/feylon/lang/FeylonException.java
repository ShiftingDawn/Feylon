package com.shiftingdawn.feylon.lang;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class FeylonException extends RuntimeException {

	@Serial
	private static final long serialVersionUID = -9188584671879490814L;
	public final List<String> additional = new ArrayList<>();

	public FeylonException(final TokenPos pos, final String msg) {
		super(FeylonException.makeError(pos, msg));
	}

	public FeylonException add(final TokenPos pos, final String msg) {
		this.additional.add(FeylonException.makeInfo(pos, msg));
		return this;
	}

	public FeylonException add(final Consumer<BiConsumer<TokenPos, String>> adder) {
		adder.accept(this::add);
		return this;
	}

	public FeylonException error(final TokenPos pos, final String msg) {
		this.additional.add(FeylonException.makeError(pos, msg));
		return this;
	}

	public FeylonException error(final Consumer<BiConsumer<TokenPos, String>> adder) {
		adder.accept(this::error);
		return this;
	}

	private void print() {
		System.err.println(this.getMessage());
		this.additional.forEach(System.err::println);
	}

	@Override
	public void printStackTrace() {
		this.print();
		super.printStackTrace();
	}

	public static String makeError(final TokenPos pos, final String msg) {
		return "%s:%d:%d: ERROR: %s".formatted(pos.file(), pos.line() + 1, pos.pos() + 1, msg);
	}

	public static String makeInfo(final TokenPos pos, final String msg) {
		return "%s:%d:%d: INFO: %s".formatted(pos.file(), pos.line() + 1, pos.pos() + 1, msg);
	}
}