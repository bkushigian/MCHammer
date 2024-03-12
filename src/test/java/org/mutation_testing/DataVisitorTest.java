package org.mutation_testing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mutation_testing.ExpressionData.DATA_KEY;

import org.junit.Before;
import org.junit.Test;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

public class DataVisitorTest {
    DataVisitor v = new DataVisitor();

    @Before
    public void setUp() {
        CombinedTypeSolver typeSolver = new CombinedTypeSolver();
        typeSolver.add(new ReflectionTypeSolver());
        StaticJavaParser.getParserConfiguration().setSymbolResolver(new JavaSymbolSolver(typeSolver));
        MCs.setOptimize(true);
    }

    MethodDeclaration getMethod(String p) {
        CompilationUnit cu = StaticJavaParser.parse(p);
        cu.accept(v, null);
        MethodDeclaration md = cu.findFirst(MethodDeclaration.class).get();
        return md;
    }


    @Test
    public void test01() {
        String p = "class A {int foo(int a, int b) {return a + b;}}";
        MethodDeclaration md = getMethod(p);
        BinaryExpr e = md.findFirst(BinaryExpr.class).get();
        ExpressionData d = e.getData(DATA_KEY);
        assertEquals(e, d.originalNode);
        assertTrue(d.type.isPrimitive());
    }
}
