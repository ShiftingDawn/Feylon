package com.shiftingdawn.feylon.lang;

public record LexedPositionalToken(TokenPos pos, LexedTokenType type, String txt) {
}
