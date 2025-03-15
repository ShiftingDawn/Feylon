package com.shiftingdawn.stacklang;

public record Instruction(Tuple<Ops, Object> op, long pos, long next) {
}