package com.shiftingdawn.feylon.syntax;

import com.shiftingdawn.feylon.lang.TokenPos;

public record FunctionDef(TokenPos pos, int pointer, PositionedType[] inputs, PositionedType[] outputs) {
}
