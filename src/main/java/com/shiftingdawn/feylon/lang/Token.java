package com.shiftingdawn.feylon.lang;

public record Token(TokenPos pos, TokenType type, String txt, Object data) {
}