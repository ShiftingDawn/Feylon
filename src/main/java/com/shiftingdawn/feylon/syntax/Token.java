package com.shiftingdawn.feylon.syntax;

public record Token(TokenPos pos, TokenType type, String txt, Object operand) {
}