package org.mutation_testing;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

public class TestUtils {
    static {
        CombinedTypeSolver typeSolver = new CombinedTypeSolver();
        typeSolver.add(new ReflectionTypeSolver());
        StaticJavaParser.getParserConfiguration().setSymbolResolver(new JavaSymbolSolver(typeSolver));
        MCs.setOptimize(true);
    }
    
    /**
     * Parse the following string into a CompilationUnit, visit it with the
     * DataVisitor, and return the first MethodDeclaration in the class.
     * @param p
     * @return
     */
    public static MethodDeclaration getMethod(String p) {
        CompilationUnit cu = StaticJavaParser.parse(p);
        cu.accept(new DataVisitor(), null);
        MethodDeclaration md = cu.findFirst(MethodDeclaration.class).get();
        return md;
    }

    public static String makeClassFromExpr(String retType, String name, String expr, String... params) {
        String paramStr = String.join(", ", params);
        return "class TestClass {\n" +
                "    " + retType + " " + name + "(" + paramStr + ") {\n" +
                "        return " + expr + ";\n" +
                "    }\n" +
                "}";
    }

    public static String makeClassFromBody(String retType, String name, String body, String...params) {
        String paramStr = String.join(", ", params);
        String rawClass =  "class TestClass {\n" +
                "    " + retType + " " + name + "(" + paramStr + ") {\n" +
                "        " + body + "\n" +
                "    }\n" +
                "}";
        
        String parsedAndPrinted = StaticJavaParser.parse(rawClass).toString();
        return parsedAndPrinted;
    }

    public static void printProgramWithLinenumbers(String p) {
        String[] lines = p.split("\n");
        for (int i = 0; i < lines.length; i++) {
            System.out.println(i+1 + ": " + lines[i]);
        }
    } 

}
