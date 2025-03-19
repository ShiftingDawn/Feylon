package com.shiftingdawn.feylon.lang;

import com.shiftingdawn.feylon.syntax.DataType;

public record FunctionType(DataType[] inputs, DataType[] outputs) {
}
