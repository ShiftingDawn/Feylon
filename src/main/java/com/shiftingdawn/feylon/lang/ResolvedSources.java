package com.shiftingdawn.feylon.lang;

import java.util.Collection;

public record ResolvedSources(String file, Collection<String> lines) {
}