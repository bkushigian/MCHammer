package org.mutation_testing;

import org.junit.Test;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;


public class RelationalVisitorTest {
    @Test
    public void testVisitMethodDecl() {
        CompilationUnit cu = StaticJavaParser.parse("class A { boolean m(int x) { return x >= 32 && x < 127; } }");
        RelationalVisitor v = new RelationalVisitor();
        v.visit(cu, null);


    }

}
