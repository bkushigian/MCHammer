package org.mutation_testing.visitors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ThisExpr;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

public class PredicateSubjectVisitorTest {
    @Test
    public void testCollectSubjects_variable_and_field() {
        CompilationUnit cu = StaticJavaParser.parse("class A { int y = 2; int foo(int x) { return x * x + y; } }");
        Set<Expression> subjects = SubjectCollector.collectSubjects(cu);
        assertEquals(2, subjects.size());
        assertTrue(subjects.contains(new NameExpr("x")));
        assertTrue(subjects.contains(new NameExpr("y")));
    }

    @Test
    public void testCollectSubjects_method_call_scope() {
        CompilationUnit cu = StaticJavaParser
                .parse("class A { int foo(int x) { return this.bar(); } int bar() { return 2; } }");
        Set<Expression> subjects = SubjectCollector.collectSubjects(cu);
        assertEquals(1, subjects.size());
        assertTrue(subjects.contains(new ThisExpr()));
    }

    @Test
    public void testCollectSubjects_method_call_missing_scope() {
        CompilationUnit cu = StaticJavaParser
                .parse("class A { int foo(int x) { return bar(); } int bar() { return 2; } }");
        Set<Expression> subjects = SubjectCollector.collectSubjects(cu);
        assertEquals(1, subjects.size());
        assertTrue(subjects.contains(new ThisExpr()));
    }

    @Test
    public void testCollectSubjects_method_call_static() {
        // Ensure that the scope of static method calls are not included as subjects
        CompilationUnit cu = StaticJavaParser
                .parse("class A { int foo(int x) { return A.bar(); } static int bar() { return 2; } }");
        Set<Expression> subjects = SubjectCollector.collectSubjects(cu);
        System.out.println(subjects);
        assertTrue(subjects.isEmpty());
    }

    @Test
    public void testCollectSubjects_method_call_args() {
        CompilationUnit cu = StaticJavaParser
                .parse("class A { int foo(int a) { return bar(a, 2); } int bar(int x, int y) { return x + y; } }");
        Set<Expression> subjects = SubjectCollector.collectSubjects(cu);
        assertEquals("Expected {this, x, y, a} but found " + subjects.toString(), 4, subjects.size());
        assertTrue(subjects.contains(new NameExpr("a")));
        assertTrue(subjects.contains(new NameExpr("x")));
        assertTrue(subjects.contains(new NameExpr("y")));
        assertTrue(subjects.contains(new ThisExpr()));
    }

    @Before
    public void setUp() throws Exception {
        CombinedTypeSolver typeSolver = new CombinedTypeSolver();
        typeSolver.add(new ReflectionTypeSolver());
        StaticJavaParser.getParserConfiguration().setSymbolResolver(new JavaSymbolSolver(typeSolver));
    }

}
