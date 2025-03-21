package com.shiftingdawn.feylon.lang;

import com.shiftingdawn.feylon.OrderedList;

public final class FunctionRef {

	public final OrderedList<TypedPos> inputs;
	public final OrderedList<TypedPos> outputs;
	public int pointer;

	public FunctionRef(final OrderedList<TypedPos> inputs, final OrderedList<TypedPos> outputs) {
		this.inputs = inputs;
		this.outputs = outputs;
	}
}
