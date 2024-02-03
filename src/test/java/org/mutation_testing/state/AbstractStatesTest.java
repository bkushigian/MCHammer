package org.mutation_testing.state;

import static org.junit.Assert.*;
import static org.mutation_testing.state.PuncturedIntervals.Interval;

public class AbstractStatesTest {
        Interval I(Long lowerBound, Long upperBound, Long... punctures) {
                return new Interval(lowerBound, upperBound, punctures);
        }

        @org.junit.Test
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

}
