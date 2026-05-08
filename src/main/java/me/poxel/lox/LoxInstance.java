package me.poxel.lox;

import java.util.HashMap;
import java.util.Map;


public class LoxInstance {

	final LoxClass klass;

	private final Map<String, Object> fields = new HashMap<>();

	public LoxInstance(LoxClass klass) {
		this.klass = klass;
	}

	@Override
	public String toString() {
		return klass.name + " instance";
	}

	void set(Token name, Object value) {
		fields.put(name.lexeme, value);
	}

	Object get(Token name) {
		if (fields.containsKey(name.lexeme)) {
			return fields.get(name.lexeme);
		}

		LoxFunction method = klass.findMethod(name.lexeme);
		if (method != null) {
			return method.bind(this);
		}

		throw new RuntimeError(name, "Undefined property '%s'.".formatted(name.lexeme));
	}
}
