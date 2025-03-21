package com.shiftingdawn.feylon.tests;

import com.shiftingdawn.feylon.Main;
import com.shiftingdawn.feylon.Simulator;
import com.shiftingdawn.feylon.lang.AssembledProgram;
import com.shiftingdawn.feylon.lang.CompilerException;
import com.shiftingdawn.feylon.lang.Feylon;
import com.shiftingdawn.feylon.lang.ResolvedSources;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;

public class ProgramTests {

	private static final String[] TESTS = new String[]{
			"parser_test_basic",
			"parser_test_blocks",
			"parser_test_consts",
	};

	@ParameterizedTest
	@MethodSource("provider")
	public void testProgram(final ResolvedSources sources) {
		final String expectedOutput = ((ArrayList<String>) sources.lines()).getFirst();
		if (!expectedOutput.startsWith("//")) {
			throw new RuntimeException("First line should be a comment with the expected output");
		}
		final var sysOut = System.out;
		final ByteArrayOutputStream boas = new ByteArrayOutputStream();
		System.setOut(new PrintStream(boas, true));
		try {
			final AssembledProgram program = Feylon.parse(sources, 0);
			new Simulator().execute(program);
		} catch (final CompilerException ex) {
			ex.printStackTrace();
			throw ex;
		}
		System.setOut(sysOut);
		final String capturedOutput = boas.toString();
		Assertions.assertEquals(expectedOutput.substring(2), capturedOutput.replaceAll("\r\n|\n", " ").trim());
	}

	private static Stream<ResolvedSources> provider() {
		return Arrays.stream(ProgramTests.TESTS).map(testName -> {
			final String path = "/feylon/" + testName + ".fey";
			final URL res = Main.class.getResource(path);
			if (res == null) {
				throw new RuntimeException("Test does not exist: " + path);
			}
			try {
				return new ResolvedSources(path, new ArrayList<>(Files.readAllLines(Paths.get(res.toURI()))));
			} catch (final IOException | URISyntaxException e) {
				throw new RuntimeException(e);
			}
		});
	}
}