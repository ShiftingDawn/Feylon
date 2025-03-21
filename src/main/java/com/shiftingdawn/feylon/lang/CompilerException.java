package com.shiftingdawn.feylon.lang;

import java.io.Serial;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class CompilerException extends AssertionError {

	@Serial
	private static final long serialVersionUID = -6578738322980705074L;
	public final CompilerErrors code;
	private final List<Map.Entry<TokenPos, String>> additionalInfo = new ArrayList<>();

	public CompilerException(final TokenPos pos, final CompilerErrors code, final String message) {
		super("%s: ERROR[%s]: %s".formatted(pos, code, message));
		this.code = code;
	}

	public CompilerException add(final TokenPos pos, final String msg) {
		this.additionalInfo.addLast(new AbstractMap.SimpleEntry<>(pos, msg));
		return this;
	}

	public CompilerException add(final Consumer<BiConsumer<TokenPos, String>> consumer) {
		consumer.accept((pos, msg) -> this.additionalInfo.addLast(new AbstractMap.SimpleEntry<>(pos, msg)));
		return this;
	}

	private void print() {
		System.err.println(this.getMessage());
		for (final Map.Entry<TokenPos, String> additional : this.additionalInfo) {
			System.err.printf("%s: INFO: %s%n", additional.getKey(), additional.getValue());
		}
	}

	@Override
	public void printStackTrace() {
		this.print();
		super.printStackTrace();
	}
}