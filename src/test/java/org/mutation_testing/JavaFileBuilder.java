package org.mutation_testing;
import java.util.*;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;


public class JavaFileBuilder {
    List<String> imports = new ArrayList<>();
    List<String> fields = new ArrayList<>();
    List<String> methods = new ArrayList<>();
    String classname;

    public JavaFileBuilder(String classname) {
        this.classname = classname;
    }

    public JavaFileBuilder addImport(String importPath) {
        imports.add("import " + importPath);
        return this;
    }

    public JavaFileBuilder addStaticImport(String importPath) {
        imports.add("import static " + importPath);
        return this;
    }

    public JavaFileBuilder addField(String type, String field) {
        fields.add(field);
        return this;
    }

    public JavaFileBuilder addField(String access, String type, String field, String init) {
        fields.add(access + " " + type + " " + field + " = " + init + ";");
        return this;
    }

    public JavaFileBuilder addMethod(String type, String name, String[] args,  String body) {
        String joinedArgs = String.join(", ", args);
        methods.add(type + " " + name + "(" + joinedArgs + ")" + "{ " + body + " }");
        return this;
    }

    String asRawString() {
        StringBuilder sb = new StringBuilder();
        for (String i : imports) {
            sb.append(i);
            sb.append(";\n");
        }
        sb.append("public class " + classname + " {\n");
        for (String f : fields) {
            sb.append(f);
            sb.append("\n");
        }
        for (String m : methods) {
            sb.append(m);
            sb.append("\n");
        }
        sb.append("}");
        return sb.toString();
    }

    CompilationUnit toCompilationUnit() {
        return StaticJavaParser.parse(asRawString());
    }

    @Override
    public String toString() {
        return toCompilationUnit().toString();
    }
}
