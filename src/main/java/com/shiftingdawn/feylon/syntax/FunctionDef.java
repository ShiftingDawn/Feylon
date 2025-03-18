package com.shiftingdawn.feylon.syntax;

public record FunctionDef(TokenPos pos, int pointer, DataType[] inputs, DataType[] outputs) {
}
