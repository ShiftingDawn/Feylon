package com.shiftingdawn.feylon.syntax;

public record FunctionDef(TokenPos pos, int pointer, PositionedType[] inputs, PositionedType[] outputs) {
}
