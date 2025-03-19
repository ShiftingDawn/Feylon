package com.shiftingdawn.feylon.syntax;

import com.shiftingdawn.feylon.lang.TokenPos;

public record PositionedType(DataType type, TokenPos pos) {
}
