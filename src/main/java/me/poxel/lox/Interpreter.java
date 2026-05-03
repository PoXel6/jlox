package me.poxel.lox;

public class Interpreter implements Expr.Visitor<Object> {

	public void interpret(Expr expression) {
		Object value = evaluate(expression);
		try {
			System.out.println(stringify(value));
		} catch (RuntimeError err) {
			Lox.runtimeError(err);
		}
	}

	@Override
	public Object visitBinaryExpr(Expr.Binary expr) {
		Object left = evaluate(expr.left);
		Object right = evaluate(expr.right);

		return switch (expr.operator.type) {
			case BANG_EQUAL -> !isEqual(left, right);
			case EQUAL_EQUAL -> isEqual(left, right);
			case GREATER -> {
				checkNumberOperand(expr.operator, left, right);
				yield (double) left > (double) right;
			}
			case GREATER_EQUAL -> {
				checkNumberOperand(expr.operator, left, right);
				yield (double) left >= (double) right;
			}
			case LESS -> {
				checkNumberOperand(expr.operator, left, right);
				yield (double) left < (double) right;
			}
			case LESS_EQUAL -> {
				checkNumberOperand(expr.operator, left, right);
				yield (double) left <= (double) right;
			}
			case MINUS -> {
				checkNumberOperand(expr.operator, right);
				yield (double) left - (double) right;
			}
			case PLUS -> {
				if (left instanceof Double leftDouble && right instanceof Double rightDouble) {
					yield leftDouble + rightDouble;
				} else if (left instanceof String leftString
				           && right instanceof String rightString) {
					yield leftString + rightString;
				} else {
					throw new RuntimeError(expr.operator,
					                       "Operand must be two numbers or two strings.");
				}
			}
			case SLASH -> {
				checkNumberOperand(expr.operator, left, right);
				yield (double) left / (double) right;
			}
			case STAR -> {
				checkNumberOperand(expr.operator, left, right);
				yield (double) left * (double) right;
			}
			default -> null;
		};
	}

	@Override
	public Object visitGroupingExpr(Expr.Grouping expr) {
		return evaluate(expr.expression);
	}

	@Override
	public Object visitLiteralExpr(Expr.Literal expr) {
		return expr.value;
	}

	@Override
	public Object visitUnaryExpr(Expr.Unary expr) {
		Object right = evaluate(expr.right);

		return switch (expr.operator.type) {
			case BANG -> !isTruthy(right);
			case MINUS -> -(double) right;
			default -> null;
		};
	}

	private String stringify(Object object) {
		if (object == null) {
			return "nil";
		}
		if (object instanceof Double) {
			String text = object.toString();
			if (text.endsWith(".0")) {
				text = text.substring(0, text.length() - 2);
			}
			return text;
		}
		return object.toString();
	}

	private void checkNumberOperand(Token operator, Object left, Object right) {
		if (left instanceof Double && right instanceof Double) {
			return;
		}
		throw new RuntimeError(operator, "Operand must be a number.");
	}

	private void checkNumberOperand(Token operator, Object operand) {
		if (operand instanceof Double) {
			return;
		}
		throw new RuntimeError(operator, "Operand must be a number.");
	}

	private boolean isEqual(Object left, Object right) {
		if (left == null && right == null) {
			return true;
		}
		if (left == null) {
			return false;
		}
		return left.equals(right);
	}

	private Object evaluate(Expr expr) {
		return expr.accept(this);
	}

	private boolean isTruthy(Object object) {
		return switch (object) {
			case null -> false;
			case Boolean bool -> bool;
			default -> true;
		};
	}

}
