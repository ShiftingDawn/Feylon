package com.shiftingdawn.feylon.lang;

import com.shiftingdawn.feylon.OrderedList;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Feylon {

	public static AssembledProgram parse(final ResolvedSources sources, final int allowedShutdownStackOverflow) {
		final OrderedList<LexedToken> lexedTokens = Lexer.lex(sources);
		final ParserContext tokenized = Tokenizer.tokenize(lexedTokens);
		Evaluator.evaluate(tokenized);
		final LinkerContext linked = Linker.link(tokenized);
		TypeChecker.check(linked, allowedShutdownStackOverflow);
		return Assembler.assemble(linked);
	}

	public static ResolvedSources readSources(final String file, final String relativeParent) throws IOException {
		final Path path = relativeParent == null ? Paths.get(file) : Paths.get(relativeParent).resolve(file);
		if (!Files.exists(path)) {
			throw new FileNotFoundException(file);
		}
		if (!Files.isReadable(path)) {
			throw new AccessDeniedException(file);
		}
		return new ResolvedSources(path.toString(), Files.readAllLines(path));
	}
}
