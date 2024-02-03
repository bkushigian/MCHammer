package org.mutation_testing.state;

import static org.junit.Assert.*;
import static org.mutation_testing.state.PuncturedIntervals.Interval;

import java.util.List;

import org.junit.Test;

import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.NameExpr;

public class AbstractStatesTest {
    Interval I(Long lowerBound, Long upperBound, Long... punctures) {
        return new Interval(lowerBound, upperBound, punctures);
    }

    @Test
    public void testPuncturedIntervals() {
        PuncturedIntervals pi = new PuncturedIntervals();
        assertEquals(new PuncturedIntervals(I(Long.MIN_VALUE, Long.MAX_VALUE)), pi);
        pi.splitAt(1L);
        assertEquals(new PuncturedIntervals(
                I(Long.MIN_VALUE, 0L),
                I(1L, 1L),
                I(2L, Long.MAX_VALUE)), pi);

        pi.splitAt(1L);
        assertEquals(new PuncturedIntervals(
                I(Long.MIN_VALUE, 0L),
                I(1L, 1L),
                I(2L, Long.MAX_VALUE)), pi);

        pi.puncture(1l);

        pi.splitAt(1L);
        assertEquals(new PuncturedIntervals(
                I(Long.MIN_VALUE, 0L),
                I(1L, 1L),
                I(2L, Long.MAX_VALUE)), pi);

        pi.puncture(32l);
        assertEquals(new PuncturedIntervals(
                I(Long.MIN_VALUE, 0L),
                I(1L, 1L),
                I(2L, Long.MAX_VALUE, 32l)), pi);

        pi.puncture(32l);
        assertEquals(new PuncturedIntervals(
                I(Long.MIN_VALUE, 0L),
                I(1L, 1L),
                I(2L, Long.MAX_VALUE, 32l)), pi);

        pi.splitAt(31l);
        assertEquals(new PuncturedIntervals(
                I(Long.MIN_VALUE, 0L),
                I(1L, 1L),
                I(2L, 30L),
                I(31L, 31L),
                I(32L, Long.MAX_VALUE, 32L)), pi);

        System.out.println(pi.asStringConditions("x"));
    }

    @Test
    public void testPuncturedIntervals02() {
        PuncturedIntervals pi = new PuncturedIntervals();
        pi.puncture(1l);
        List<Expression> conditions = pi.asConditions(new NameExpr("x"));
        assertEquals(2, conditions.size());

        // This should produce 4 conditions:
        // 1. x <= 4 && x != 1
        // 2. x == 1
        // 3. x == 5
        // 4. x >= 6
        pi.splitAt(5l);
        conditions = pi.asConditions(new NameExpr("x"));
        assertEquals(conditions.toString(), 4, conditions.size());
        System.out.println("A:" + conditions.toString());

        pi.splitAt(5l);
        conditions = pi.asConditions(new NameExpr("x"));
        assertEquals(conditions.toString(), 4, conditions.size());
        System.out.println("B:" + conditions.toString());

        // This should produce 5 conditions:
        // 1. x <= 0
        // 2. x == 1
        // 3. x >= 2 && x <= 4
        // 4. x == 5
        // 5. x >= 6
        pi.splitAt(1l);
        conditions = pi.asConditions(new NameExpr("x"));
        assertEquals(conditions.toString(), 5, conditions.size());
        System.out.println("C:" + conditions.toString());


        pi.puncture(1l);
        conditions = pi.asConditions(new NameExpr("x"));
        System.out.println("D:" + conditions.toString());
        assertEquals(conditions.toString(), 5, conditions.size());

        // This should produce 7 conditions:
        // 1. x <= 0
        // 2. x == 1
        // 3. x == 2 
        // 4. x == 3
        // 5. x == 4
        // 6. x == 5
        // 7. x >= 6
        pi.splitAt(3l);
        conditions = pi.asConditions(new NameExpr("x"));
        System.out.println("E:" + conditions.toString());
        assertEquals(conditions.toString(), 7, conditions.size());
    }

}
