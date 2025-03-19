package com.shiftingdawn.feylon.lang;

import java.util.Map;
import java.util.SequencedCollection;

public record LexedProgramSource(SequencedCollection<RawToken> tokens, Map<String, FunctionType> funcs) {
}