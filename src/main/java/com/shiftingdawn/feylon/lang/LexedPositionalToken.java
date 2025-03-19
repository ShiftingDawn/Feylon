package com.shiftingdawn.feylon.lang;

import com.shiftingdawn.feylon.syntax.TokenPos;

public record LexedPositionalToken(TokenPos pos, LexedTokenType type, String txt) {
}
