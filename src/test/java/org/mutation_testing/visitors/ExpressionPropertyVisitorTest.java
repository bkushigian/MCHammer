package org.mutation_testing.visitors;

import static org.junit.Assert.*;
import static org.mutation_testing.visitors.ExpressionPropertyVisitor.Properties;

import org.mutation_testing.visitors.ExpressionPropertyVisitor;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.expr.Expression;

public class ExpressionPropertyVisitorTest {

    Expression parseExpr(String expr) {
        return StaticJavaParser.parseExpression(expr);
    }

    Properties p(String expr) {
        Expression e = parseExpr(expr);
        System.out.println(e);
        return e.accept(new ExpressionPropertyVisitor(), null);
    }

    @org.junit.Test
    public void testVisit() {
        Properties ps;
        ps = p("x < 1");
        assertTrue(ps.isSimpleRelational && !ps.isSimpleArithmetic && ps.isSimplePredicate && !ps.isSimpleLogical);
        ps = p("x + 1");
        assertTrue(!ps.isSimpleRelational && ps.isSimpleArithmetic && !ps.isSimplePredicate && !ps.isSimpleLogical);
        ps = p("x + 1 < 2");
        assertTrue(!ps.isSimpleRelational && !ps.isSimpleArithmetic && !ps.isSimplePredicate && !ps.isSimpleLogical);
        ps = p("x < 1 && y < 2 || z < 3");
        System.out.println(ps);
        assertTrue(!ps.isSimpleRelational && !ps.isSimpleArithmetic && !ps.isSimplePredicate && ps.isSimpleLogical);
        ps = p("x < 1 && y < 2 || 5*z < 3");
        assertTrue(!ps.isSimpleRelational && !ps.isSimpleArithmetic && !ps.isSimplePredicate && !ps.isSimpleLogical);
        ps = p("x < 1 && y < 2 || z < (3 + 1)");
        assertTrue(!ps.isSimpleRelational && !ps.isSimpleArithmetic && !ps.isSimplePredicate && !ps.isSimpleLogical);
        ps = p("!(x < 1) && y < 2 || z < (3 + 1)");
        assertTrue(!ps.isSimpleRelational && !ps.isSimpleArithmetic && !ps.isSimplePredicate && !ps.isSimpleLogical);
        ps = p("!(x++ < 1) && y[3] < 2 || z < 1");
        assertTrue(!ps.isPure);

    }
}
