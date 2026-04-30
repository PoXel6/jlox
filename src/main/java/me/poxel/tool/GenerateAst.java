package me.poxel.tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;


public class GenerateAst {

	void main(String... args) throws IOException {
		if (args.length != 1) {
			System.out.println("Usage: generate_ast <output directory>");
			System.exit(64);
		}
		String outputDir = args[0];
		defineAst(outputDir,
		          "Expr",
		          Arrays.asList("Binary : Expr left, Token operator, Expr right",
		                        "Grouping : Expr expression",
		                        "Literal : Object value",
		                        "Unary : Token operator, Expr right"));

	}

	private void defineAst(String outputDir, String baseName, List<String> types)
			throws IOException {
		String path = outputDir + "/" + baseName + ".java";
		try (PrintWriter writer = new PrintWriter(path, StandardCharsets.UTF_8)) {
			writer.println("package me.poxel.lox;");
			writer.println();
			writer.println("import java.util.List;");
			writer.println();
			writer.println("public abstract class " + baseName + " {");

			defineVisitor(writer, baseName, types);

			for (final String type : types) {
				String className = type.split(":")[0].trim();
				String fields = type.split(":")[1].trim();
				defineType(writer, baseName, className, fields);
			}
			writer.println();
			writer.println("\tabstract <R> R accept(Visitor<R> visitor);");

			writer.println("}");
		}
	}

	private void defineVisitor(PrintWriter writer, String baseName, List<String> types) {
		writer.println("\tinterface Visitor<R> {");
		writer.println();
		for (final String type : types) {
			String typeName = type.split(":")[0].trim();
			writer.println("\t\t R visit" + typeName + baseName + "(" + typeName + " "
			               + baseName.toLowerCase() + ");");
			writer.println();
		}
		writer.println("\t}");
		writer.println();
	}

	private void defineType(PrintWriter writer,
	                        String baseName,
	                        String className,
	                        String fieldList) {
		writer.println("\tstatic class " + className + " extends " + baseName + " {");

		writer.println("\t\t" + className + "(" + fieldList + ") {");
		String[] fields = fieldList.split(", ");
		for (final String field : fields) {
			String name = field.split(" ")[1];
			writer.println("\t\t\tthis." + name + " = " + name + ";");
		}
		writer.println("\t\t}");

		writer.println();
		writer.println("\t\t@Override");
		writer.println("\t\t<R> R accept(Visitor<R> visitor) {");
		writer.println("\t\t\treturn visitor.visit" + className + baseName + "(this);");
		writer.println("\t\t}");

		writer.println();

		for (final String field : fields) {
			writer.println("\t\tfinal " + field + ";");
		}

		writer.println("\t}");
		writer.println();

	}
}
