package org.mutation_testing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.NameExpr;
import static com.github.javaparser.ast.expr.BinaryExpr.Operator.*;

public class MCsOptimizerTest {

    final BooleanLiteralExpr trueExpr = new BooleanLiteralExpr(true);
    final BooleanLiteralExpr falseExpr = new BooleanLiteralExpr(false);
    final NameExpr a = new NameExpr("a");
    final NameExpr b = new NameExpr("b");
    final NameExpr c = new NameExpr("c");
    final NameExpr d = new NameExpr("d");


    @Test
    public void testRelOpLogicallyEntails() {
        assertEntails(LESS, LESS);
        assertEntails(LESS_EQUALS, LESS_EQUALS);
        assertEntails(GREATER, GREATER);
        assertEntails(GREATER_EQUALS, GREATER_EQUALS);
        assertEntails(EQUALS, EQUALS);
        assertEntails(NOT_EQUALS, NOT_EQUALS);

        assertEntails(LESS, LESS_EQUALS);
        assertNotEntails(LESS_EQUALS, LESS);

        assertEntails(GREATER, GREATER_EQUALS);
        assertNotEntails(GREATER_EQUALS, GREATER);

        assertEntails(EQUALS, LESS_EQUALS);
        assertNotEntails(LESS_EQUALS, EQUALS);

        assertEntails(LESS, NOT_EQUALS);
        assertNotEntails(NOT_EQUALS, LESS);

        assertEntails(GREATER, NOT_EQUALS);
        assertNotEntails(NOT_EQUALS, GREATER);

        assertNotEntails(LESS_EQUALS, NOT_EQUALS);
        assertNotEntails(NOT_EQUALS, LESS_EQUALS);
    }

    @Test
    public void testLogicallyEntails() {
        assertEntails(falseExpr, trueExpr);
        assertNotEntails(trueExpr, falseExpr);

        assertEntails(lt(a, b), le(a,b));
        assertNotEntails(le(a, b), lt(a,b));

        assertEntails(lt(a, b), ne(a,b));
        assertNotEntails(ne(a, b), lt(a,b));

        assertEntails(eq(a,b), ge(a, b));
        assertNotEntails(ge(a, b), eq(a,b));

        assertEntails(lt(a, b), ge(b, a));
        assertEntails(lt(a, b), trueExpr);

    }

    @Test
    public void testSimplifyConjunction01() {
        Expression[] expected = new Expression[] {lt(a, b)};
        assertSimplified(expected, lt(a, b), lt(a, b));
    }

    @Test
    public void testSimplifyConjunction02() {
        Expression[] expected = new Expression[] {lt(a, b)};
        assertSimplified(expected, lt(a, b), trueExpr);
    }

    @Test
    public void testSimplifyConjunction03() {
        Expression[] expected = new Expression[] {falseExpr};
        assertSimplified(expected, lt(a, b), falseExpr);
    }

    @Test
    public void testSimplifyConjunction04() {
        Expression[] expected = new Expression[] {lt(a, b)};
        assertSimplified(expected, lt(a, b), le(a, b));
    }

    @Test
    public void testSimplifyConjunction05() {
        Expression[] expected = new Expression[] {eq(a, b)};
        assertSimplified(expected, eq(a, b), eq(a, b), eq(a, b), eq(a, b), eq(b, a));
    }

    @Test
    public void testSimplifyConjunction06() {
        Expression[] expected = new Expression[] {eq(a, b), lt(b, c)};
        assertSimplified(expected, le(a, b), eq(a, b), eq(a, b), le(b, c), lt(b, c), trueExpr);
    }

    /// BEGIN HELPER METHODS

    static BinaryExpr lt(Expression lhs, Expression rhs) {
        return new BinaryExpr(lhs, rhs, LESS);
    }

    static BinaryExpr le(Expression lhs, Expression rhs) {
        return new BinaryExpr(lhs, rhs, LESS_EQUALS);
    }

    static BinaryExpr gt(Expression lhs, Expression rhs) {
        return new BinaryExpr(lhs, rhs, GREATER);
    }

    static BinaryExpr ge(Expression lhs, Expression rhs) {
        return new BinaryExpr(lhs, rhs, GREATER_EQUALS);
    }

    static BinaryExpr eq(Expression lhs, Expression rhs) {
        return new BinaryExpr(lhs, rhs, EQUALS);
    }

    static BinaryExpr ne(Expression lhs, Expression rhs) {
        return new BinaryExpr(lhs, rhs, NOT_EQUALS);
    }

    static void assertEntails(Expression pre, Expression ant) {
        assertTrue(MCsOptimizer.logicallyEntails(pre, ant));
    }

    static void assertNotEntails(Expression pre, Expression ant) {
        assertFalse(MCsOptimizer.logicallyEntails(pre, ant));
    }

    static void assertEntails(BinaryExpr.Operator pre, BinaryExpr.Operator ant) {
        assertTrue(MCsOptimizer.relOpLogicallyEntails(pre, ant));
    }

    static void assertNotEntails(BinaryExpr.Operator pre, BinaryExpr.Operator ant) {
        assertFalse(MCsOptimizer.relOpLogicallyEntails(pre, ant));
    }

    static void assertSimplified(Expression[] expected, Expression...conj) {
        assertSimplified(Arrays.asList(expected), Arrays.asList(conj));
    }

    static void assertSimplified(List<Expression> expected, List<Expression> conj) {
        List<Expression> simplified = MCsOptimizer.simplifyConjunctionOperands(conj);
        String expectedStr = expected.toString();
        String simplifiedStr = simplified.toString();
        String errMessage = "Expected: " + expectedStr + " but got: " + simplifiedStr;
        assertTrue(errMessage, simplified.containsAll(expected));
        assertTrue(errMessage, expected.containsAll(simplified));
        assertEquals(errMessage, expected.size(), simplified.size());
    }
}
