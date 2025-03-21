package com.shiftingdawn.feylon;

import com.shiftingdawn.feylon.lang.AssembledProgram;
import com.shiftingdawn.feylon.lang.Feylon;
import com.shiftingdawn.feylon.lang.ResolvedSources;

import java.io.File;
import java.io.IOException;

public final class Main {

	public static void main(final String[] args) {
		if (args.length == 0) {
			Main.printUsage();
			System.exit(1);
		}
		if ("help".equalsIgnoreCase(args[0])) {
			Main.printUsage();
			return;
		}
		if ("sim".equalsIgnoreCase(args[0])) {
			if (args.length < 2) {
				System.err.println("Missing source path");
				System.exit(1);
			}
			final File f = new File(args[1]);
			if (!f.exists() || !f.canRead()) {
				System.err.println("File does not exist or read access was denied");
				System.exit(1);
			}
			try {
				final ResolvedSources sources = Feylon.readSources(f.getAbsolutePath(), null);
				final AssembledProgram program = Feylon.parse(sources, 0);
				new Simulator(program).execute();
			} catch (final IOException e) {
				System.err.println("Could not read file");
				e.printStackTrace();
				System.exit(1);
			}
		}
	}

	private static void printUsage() {
		System.out.println("Usage: StackLang <option> [...args]");
		System.out.println("Options:");
		System.out.println("\thelp: Print this info");
		System.out.println("\tsim: Simulate the program");
		System.out.println("\t\tRequired a file path as first argument");
	}
}