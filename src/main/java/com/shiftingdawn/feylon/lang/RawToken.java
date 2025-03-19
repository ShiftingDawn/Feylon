package com.shiftingdawn.feylon.lang;

public record RawToken(TokenPos pos, RawTokenType type, String txt, Object operand) {
}