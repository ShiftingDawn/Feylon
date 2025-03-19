package com.shiftingdawn.feylon.syntax;

import com.shiftingdawn.feylon.lang.RawToken;
import com.shiftingdawn.feylon.lang.TokenPos;

public class InstructionSource {

	public final TokenPos pos;
	public final InstructionType type;
	public final String txt;
	public Object data;

	public InstructionSource(final RawToken token, final InstructionType type, final Object data) {
		this.pos = token.pos();
		this.type = type;
		this.txt = token.txt();
		this.data = data;
	}
}