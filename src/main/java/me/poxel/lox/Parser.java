package me.poxel.lox;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;


public class Parser {

	private final List<Token> tokens;

	private int current = 0;

	public Parser(List<Token> tokens) {
		this.tokens = tokens;
	}

	List<Stmt> parse() {
		List<Stmt> statements = new ArrayList<>();
		while (!isAtEnd()) {
			statements.add(declarations());
		}
		return statements;
	}

	private Stmt declarations() {
		try {
			if (match(TokenType.FUN)) {
				return function("function");
			}
			if (match(TokenType.VAR)) {
				return varDeclaration();
			}
			return statement();
		} catch (ParseError error) {
			synchronize();
			return null;
		}
	}

	@SuppressWarnings("ThrowableNotThrown")
	private Stmt.Function function(String kind) {
		Token name = consume(TokenType.IDENTIFIER, "Expect %s name.".formatted(kind));
		consume(TokenType.LEFT_PAREN, "Expect '(' after %s name.".formatted(kind));
		List<Token> parameters = new ArrayList<>();

		if (!check(TokenType.RIGHT_PAREN)) {
			do {
				if (parameters.size() >= 255) {
					error(peek(), "Can't have more than 255 parameters.");
				}
				parameters.add(consume(TokenType.IDENTIFIER, "Expect parameter name."));
			} while (match(TokenType.COMMA));
		}
		consume(TokenType.RIGHT_PAREN, "Expect ')' after parameters.");

		consume(TokenType.LEFT_BRACE, "Expect '{' before %s body.".formatted(kind));
		List<Stmt> body = block();
		return new Stmt.Function(name, parameters, body);
	}

	private Stmt varDeclaration() {
		Token name = consume(TokenType.IDENTIFIER, "Expect variable name");

		Expr initializer = null;
		if (match(TokenType.EQUAL)) {
			initializer = expression();
		}

		consume(TokenType.SEMICOLON, "Expect ';' after variable declaration.");
		return new Stmt.Var(name, initializer);
	}

	private Stmt statement() {

		if (match(TokenType.FOR)) {
			return forStatement();
		}

		if (match(TokenType.IF)) {
			return ifStatement();
		}

		if (match(TokenType.PRINT)) {
			return printStatement();
		}

		if (match(TokenType.RETURN)) {
			return returnStatement();
		}

		if (match(TokenType.WHILE)) {
			return whileStatement();
		}

		if (match(TokenType.LEFT_BRACE)) {
			return new Stmt.Block(block());
		}

		return expressionStatement();
	}

	private Stmt returnStatement() {
		Token keyword = previous();
		Expr value = null;
		if (!check(TokenType.SEMICOLON)) {
			value = expression();
		}
		consume(TokenType.SEMICOLON, "Expect ';' after return value.");
		return new Stmt.Return(keyword, value);
	}

	private Stmt forStatement() {
		consume(TokenType.LEFT_PAREN, "Expect '(' after 'for'.");

		Stmt initializer = switch (advance().type) {
			case TokenType.SEMICOLON -> null;
			case TokenType.VAR -> varDeclaration();
			default -> expressionStatement();
		};

		// This is book code, I have kept this in case I messed something up.
		//		Stmt initializer;
		//		if (match(TokenType.SEMICOLON)) {
		//			initializer = null;
		//		} else if (match(TokenType.VAR)) {
		//			initializer = varDeclaration();
		//		} else {
		//			initializer = expressionStatement();
		//		}

		Expr condition = null;
		if (!check(TokenType.SEMICOLON)) {
			condition = expression();
		}
		consume(TokenType.SEMICOLON, "Expect ';' after loop condition.");

		Expr increment = null;
		if (!check(TokenType.RIGHT_PAREN)) {
			increment = expression();
		}

		consume(TokenType.RIGHT_PAREN, "Expect ')' after 'for'.");

		Stmt body = statement();
		if (increment != null) {
			body = new Stmt.Block(List.of(body, new Stmt.Expression(increment)));
		}

		if (condition == null) {
			condition = new Expr.Literal(true);
		}

		body = new Stmt.While(condition, body);

		if (initializer != null) {
			body = new Stmt.Block(List.of(initializer, body));
		}

		return body;
	}

	private Stmt whileStatement() {
		consume(TokenType.LEFT_PAREN, "Expect '(' after 'while'.");
		Expr condition = expression();
		consume(TokenType.RIGHT_PAREN, "Expect ')' after 'while' condition.");
		Stmt body = statement();

		return new Stmt.While(condition, body);
	}

	private Stmt ifStatement() {
		consume(TokenType.LEFT_PAREN, "Expect '(' after 'if'.");
		Expr condition = expression();
		consume(TokenType.RIGHT_PAREN, "Expect ')' after 'if' condition.");

		Stmt thenBranch = statement();
		Stmt elseBranch = null;

		if (match(TokenType.ELSE)) {
			elseBranch = statement();
		}

		return new Stmt.If(condition, thenBranch, elseBranch);
	}

	private List<Stmt> block() {
		List<Stmt> statements = new ArrayList<>();
		while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
			statements.add(declarations());
		}
		consume(TokenType.RIGHT_BRACE, "Expect '}' after block.");
		return statements;
	}

	private Stmt expressionStatement() {
		Expr expr = expression();
		consume(TokenType.SEMICOLON, "Expect ';' after value.");
		return new Stmt.Expression(expr);
	}

	private Stmt printStatement() {
		Expr value = expression();
		consume(TokenType.SEMICOLON, "Expect ';' after value.");
		return new Stmt.Print(value);
	}

	private Expr expression() {
		return assignment();
	}

	@SuppressWarnings("ThrowableNotThrown")
	private Expr assignment() {
		Expr expr = or();

		if (match(TokenType.EQUAL)) {
			Token equals = previous();
			Expr value = assignment();

			if (expr instanceof Expr.Variable var) {
				Token name = var.name;
				return new Expr.Assign(name, value);
			}
			error(equals, "Invalid assignment target.");
		}
		return expr;
	}

	private Expr or() {
		Expr expr = and();

		while (match(TokenType.OR)) {
			Token operator = previous();
			Expr right = and();
			expr = new Expr.Logical(expr, operator, right);
		}

		return expr;
	}

	private Expr and() {
		Expr expr = equality();

		while (match(TokenType.AND)) {
			Token operator = previous();
			Expr right = equality();
			expr = new Expr.Logical(expr, operator, right);
		}

		return expr;
	}

	private Expr equality() {
		Expr expr = comparison();
		while (match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
			Token operator = previous();
			Expr right = comparison();
			expr = new Expr.Binary(expr, operator, right);
		}
		return expr;
	}

	private Expr comparison() {
		Expr expr = term();
		while (match(TokenType.LESS,
				TokenType.LESS_EQUAL,
				TokenType.GREATER,
				TokenType.GREATER_EQUAL)) {
			Token operator = previous();
			Expr right = term();
			expr = new Expr.Binary(expr, operator, right);
		}
		return expr;
	}

	private Expr term() {
		Expr expr = factor();
		while (match(TokenType.MINUS, TokenType.PLUS)) {
			Token operator = previous();
			Expr right = factor();
			expr = new Expr.Binary(expr, operator, right);
		}
		return expr;
	}

	private Expr factor() {
		Expr expr = unary();
		while (match(TokenType.SLASH, TokenType.STAR)) {
			Token operator = previous();
			Expr right = unary();
			expr = new Expr.Binary(expr, operator, right);
		}
		return expr;
	}

	private Expr unary() {
		if (match(TokenType.BANG, TokenType.MINUS)) {
			Token operator = previous();
			Expr right = unary();
			return new Expr.Unary(operator, right);
		}
		return call();
	}

	private Expr call() {
		Expr expr = primary();
		while (true) {
			if (match(TokenType.LEFT_PAREN)) {
				expr = finishCall(expr);
			} else {
				break;
			}
		}
		return expr;
	}

	@SuppressWarnings("ThrowableNotThrown")
	private Expr finishCall(Expr callee) {
		List<Expr> arguments = new ArrayList<>();

		if (!check(TokenType.RIGHT_PAREN)) {
			do {
				if (arguments.size() >= 255) {
					error(peek(), "Can't have more than 255 arguments.");
				}
				arguments.add(expression());
			} while (match(TokenType.COMMA));
		}

		Token paren = consume(TokenType.RIGHT_PAREN, "Expect ')' after arguments.");
		return new Expr.Call(callee, paren, arguments);
	}

	private Expr primary() {
		if (match(TokenType.FALSE)) {
			return new Expr.Literal(false);
		}

		if (match(TokenType.TRUE)) {
			return new Expr.Literal(true);
		}

		if (match(TokenType.NIL)) {
			return new Expr.Literal(null);
		}

		if (match(TokenType.NUMBER, TokenType.STRING)) {
			return new Expr.Literal(previous().literal);
		}

		if (match(TokenType.IDENTIFIER)) {
			return new Expr.Variable(previous());
		}

		if (match(TokenType.LEFT_PAREN)) {
			Expr expr = expression();
			consume(TokenType.RIGHT_PAREN, "Expect ')' after expression.");
			return new Expr.Grouping(expr);
		}

		throw error(peek(), "Expect expression.");
	}

	private Token consume(TokenType type, String message) {
		if (check(type)) {
			return advance();
		}
		throw error(peek(), message);
	}

	private ParseError error(Token token, String message) {
		Lox.error(token, message);
		return new ParseError();
	}

	private void synchronize() {
		advance();

		while (!isAtEnd()) {
			if (previous().type == TokenType.SEMICOLON) {
				return;
			}
			switch (peek().type) {
				case CLASS:
				case FUN:
				case VAR:
				case FOR:
				case IF:
				case WHILE:
				case PRINT:
				case RETURN:
					return;
			}
			advance();
		}
	}

	private Expr grammarHelper(Supplier<Expr> expression, TokenType... types) {
		Expr expr = expression.get();
		while (match(types)) {
			Token operator = previous();
			Expr right = expression.get();
			expr = new Expr.Binary(expr, operator, right);
		}
		return expr;
	}

	private Token previous() {
		return tokens.get(current - 1);
	}

	private boolean match(TokenType... types) {
		for (final TokenType type : types) {
			if (check(type)) {
				advance();
				return true;
			}
		}
		return false;
	}

	private Token advance() {
		if (!isAtEnd()) {
			current++;
		}
		return previous();
	}

	private boolean isAtEnd() {
		return peek().type == TokenType.EOF;
	}

	private Token peek() {
		return tokens.get(current);
	}

	private boolean check(TokenType type) {
		if (isAtEnd()) {
			return false;
		}
		return peek().type == type;
	}

	private static class ParseError extends RuntimeException {}

}
