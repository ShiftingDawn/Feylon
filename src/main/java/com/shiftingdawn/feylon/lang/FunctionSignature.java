package com.shiftingdawn.feylon.lang;

import com.shiftingdawn.feylon.OrderedList;

public record FunctionSignature(OrderedList<TypedPos> inputs, OrderedList<TypedPos> outputs) {
}
