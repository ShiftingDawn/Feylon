package com.shiftingdawn.feylon.lang;

import com.shiftingdawn.feylon.Main;
import com.shiftingdawn.feylon.OrderedList;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

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
		if (file.startsWith("class:")) {
			final URL resource = Main.class.getResource(file.substring(6));
			if (resource == null) {
				throw new FileNotFoundException(file);
			}
			try {
				return new ResolvedSources(file, new ArrayList<>(Files.readAllLines(Paths.get(resource.toURI()))));
			} catch (final URISyntaxException e) {
				throw new IOException(e);
			}
		} else {
			final Path path = relativeParent == null ? Paths.get(file) : Paths.get(relativeParent).getParent().resolve(file);
			if (!Files.exists(path)) {
				throw new FileNotFoundException(path.toString());
			}
			if (!Files.isReadable(path)) {
				throw new AccessDeniedException(path.toString());
			}
			return new ResolvedSources(path.toString(), Files.readAllLines(path));
		}
	}
}
