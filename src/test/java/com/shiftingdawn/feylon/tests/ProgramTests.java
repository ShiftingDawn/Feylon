package com.shiftingdawn.feylon.tests;

import com.shiftingdawn.feylon.Simulator;
import com.shiftingdawn.feylon.syntax.Compiler;
import com.shiftingdawn.feylon.syntax.CompilerException;
import com.shiftingdawn.feylon.syntax.Program;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

public class ProgramTests {

	private static final String[] TESTS = new String[]{
			"parser_test_basic",
			"parser_test_blocks",
			"parser_test_consts",
	};

	@ParameterizedTest
	@MethodSource("provider")
	public void testProgram(final Map.Entry<String, ArrayList<String>> programSource) {
		final String expectedOutput = programSource.getValue().getFirst();
		if (!expectedOutput.startsWith("//")) {
			throw new RuntimeException("First line should be a comment with the expected output");
		}
		final var sysOut = System.out;
		final ByteArrayOutputStream boas = new ByteArrayOutputStream();
		System.setOut(new PrintStream(boas, true));
		try {
			final Program program = Compiler.compile(programSource.getKey(), programSource.getValue());
			new Simulator().execute(program);
		} catch (final CompilerException ex) {
			ex.printStackTrace();
			throw ex;
		}
		System.setOut(sysOut);
		final String capturedOutput = boas.toString();
		Assertions.assertEquals(expectedOutput.substring(2), capturedOutput.replaceAll("\r\n|\n", " ").trim());
	}

	private static Stream<Map.Entry<String, Collection<String>>> provider() {
		return Arrays.stream(ProgramTests.TESTS).map(testName -> {
			final String path = "/feylon/" + testName + ".fey";
			final URL res = Program.class.getResource(path);
			if (res == null) {
				throw new RuntimeException("Test does not exist: " + path);
			}
			try {
				return new AbstractMap.SimpleEntry<>(path, new ArrayList<>(Files.readAllLines(Paths.get(res.toURI()))));
			} catch (final IOException | URISyntaxException e) {
				throw new RuntimeException(e);
			}
		});
	}
}