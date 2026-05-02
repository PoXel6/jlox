package me.poxel.lox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;


public class Lox {

	static boolean hadError = false;

	public void main(String... args) throws IOException {
		if (args.length > 1) {
			System.out.println("Usage: jlox [script]");
			System.exit(64);
		} else if (args.length == 1) {
			runFile(args[0]);
		} else {
			runPrompt();
		}
	}

	static void error(Token token, String message) {
		if (token.type == TokenType.EOF) {
			report(token.line, " at end", message);
		} else {
			report(token.line, " at '" + token.lexeme + "'", message);
		}
	}

	static void error(int line, String message) {
		report(line, "", message);
	}

	static void report(int line, String where, String message) {
		System.out.printf("[line %d] Error %s: %s%n", line, where, message);
	}

	private void runPrompt() throws IOException {
		InputStreamReader input = new InputStreamReader(System.in);
		BufferedReader reader = new BufferedReader(input);

		for (; ; ) {
			System.out.print("> ");
			String line = reader.readLine();
			if (line == null) {
				break;
			}
			run(line);
			hadError = false;
		}
	}

	private void runFile(String filePath) throws IOException {
		byte[] bytes = Files.readAllBytes(Paths.get(filePath));
		run(new String(bytes, Charset.defaultCharset()));

		if (hadError) {
			System.exit(64);
		}
	}

	private void run(String source) {
		Scanner scanner = new Scanner(source);
		List<Token> tokens = scanner.scanTokens();
		Parser parser = new Parser(tokens);
		Expr expression = parser.parse();

		if (hadError) {
			return;
		}

		System.out.println(new AstPrinter().print(expression));
	}
}
