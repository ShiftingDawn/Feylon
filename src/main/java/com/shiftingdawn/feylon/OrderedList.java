package com.shiftingdawn.feylon;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Collection;

public class OrderedList<E> extends ArrayList<E> {

	public OrderedList() {
	}

	public OrderedList(final Collection<? extends E> c) {
		super(c);
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
}