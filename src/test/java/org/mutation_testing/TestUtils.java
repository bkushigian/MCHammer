package org.mutation_testing;

import java.util.StringJoiner;

public class TestUtils {

    /**
     * A helper method to create a class with a single method wrapping the
     * expression passed in. This wrapper method will define each variable's type
     * according to the args passed in.
     * 
     * @param expr          the expression to wrap
     * @param variableTypes "int a", "char c", "String s", etc
     * @return
     */
    public static String makeClass(String expr, String... variableTypes) {
        StringJoiner argJoiner = new StringJoiner(", ");
        for (String arg : variableTypes) {
            argJoiner.add(arg);
        }
        return "class TestClass {\n" +
                "boolean f(" + argJoiner + ") {\n" +
                "return " + expr + ";\n" +
                "}\n" +
                "}";
    }
}