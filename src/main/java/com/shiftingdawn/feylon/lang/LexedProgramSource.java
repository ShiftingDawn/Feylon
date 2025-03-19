package com.shiftingdawn.feylon.lang;

import java.util.Map;
import java.util.SequencedCollection;

import com.shiftingdawn.feylon.syntax.Token;

public record LexedProgramSource(SequencedCollection<Token> tokens, Map<String, FunctionType> funcs) {
}