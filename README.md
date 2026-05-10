*jlox*, a lox interpreter written by me in Java.
Based on the book [Crafting Interpreters](https://github.com/munificent/craftinginterpreters/).

# JLOX
### Language Features:
---

- tokens and lexing
- recursive descent parsing
- abstract syntax trees
- prefix and infix expressions
- runtime representation of objects
- interpreting code using the Visitor pattern
- lexical scope
- environment chains for storing variables
- control flow
- functions with parameters
- closures
- static variable resolution and error detection
- classes
- constructors
- fields
- methods
- inheritance.

### Deviations from the Book
---
- Utilized modern Java features like pattern matching and switch expressions.

### Build
---
```bash
./gradlew build # builds the project.

./gradlew clean # clear previous build files.

./gradlew run # Broken: Gradle doesn't like infinite loop so it won't run then.

./gradlew run --args="./examples/<example-name>.lox" # to run specific example.
```

### Examples
---
Few basic examples are provided in [example folder](./examples) of this repository.
- `This.lox` how `this` works in Lox (spoiler: just like it does in Java).
- `Inheritance.lox` how `inheritance` works in Lox.
- `Fib.lox` simple Fibonacci number calculator; prints the first 30.