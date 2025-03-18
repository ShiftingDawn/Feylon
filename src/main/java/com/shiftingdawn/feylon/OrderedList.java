package com.shiftingdawn.feylon;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class OrderedList<E> extends ArrayList<E> {

	public OrderedList() {
	}

	public OrderedList(final Collection<? extends E> c) {
		super(c);
	}

	public OrderedList(final E[] arr) {
		super(Arrays.asList(arr));
	}

	@Serial
	private static final long serialVersionUID = -5206034074738876403L;

	public void append(final E element) {
		this.addLast(element);
	}

	public E pop() {
		final E result = this.getLast();
		this.removeLast();
		return result;
	}

	public OrderedList<E> reverse() {
		Collections.reverse(this);
		return this;
	}
}