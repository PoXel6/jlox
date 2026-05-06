package me.poxel.lox;

import java.util.List;


public class NativeFunctions {

	static class Clock implements LoxCallable {

		@Override
		public Object call(Interpreter interpreter, List<Object> arguments) {
			return (double) System.currentTimeMillis() / 1000.0;
		}

		@Override
		public int arity() {
			return 0;
		}

		@Override
		public String toString() {
			return "<native fn>";
		}
	}

	static class Scan implements LoxCallable {

		@Override
		public Object call(Interpreter interpreter, List<Object> arguments) {
			return IO.readln();
		}

		@Override
		public int arity() {
			return 0;
		}

		@Override
		public String toString() {
			return "<native fn>";
		}
	}

	static class PrintLine implements LoxCallable {

		@Override
		public Object call(Interpreter interpreter, List<Object> arguments) {
			System.out.println(interpreter.stringify(arguments.getFirst()));
			return null;
		}

		@Override
		public int arity() {
			return 1;
		}

		@Override
		public String toString() {
			return "<native fn>";
		}
	}
}
