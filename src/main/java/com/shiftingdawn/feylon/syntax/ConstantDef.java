package com.shiftingdawn.feylon.syntax;

import com.shiftingdawn.feylon.lang.TokenPos;

public record ConstantDef(TokenPos pos, DataType dataType, int value) {
}
