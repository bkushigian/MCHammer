package org.mutation_testing.state;

import java.util.ArrayList;
import java.util.List;

import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.LongLiteralExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SimpleName;

public class PuncturedIntervals {

    List<PuncturedIntervals.Interval> intervals;

    public PuncturedIntervals() {
        this.intervals = new ArrayList<>();
        intervals.add(new Interval(Long.MIN_VALUE, Long.MAX_VALUE));
    }

    public PuncturedIntervals(PuncturedIntervals.Interval... intervals) {
        this.intervals = new ArrayList<>();
        for (PuncturedIntervals.Interval interval : intervals) {
            this.intervals.add(interval);
        }
    }

    public void puncture(Long point) {
        if (point == null) {
            throw new IllegalArgumentException("Point cannot be null");
        }
        for (PuncturedIntervals.Interval interval : intervals) {
            interval.puncture(point);
        }
    }

    public void splitAt(Long point) {
        if (point == null) {
            throw new IllegalArgumentException("Point cannot be null");
        }
        List<PuncturedIntervals.Interval> newIntervals = new ArrayList<>();
        for (PuncturedIntervals.Interval interval : intervals) {
            newIntervals.addAll(interval.splitAt(point));
        }
        intervals = newIntervals;
    }

    public List<Expression> asConditions(Expression expr) {
        List<Expression> conditions = new ArrayList<>();
        for (PuncturedIntervals.Interval interval : intervals) {
            conditions.addAll(interval.asConditions(expr));
        }

        return conditions;
    }

    public List<String> asStringConditions(String variableName) {
        List<String> stringConditions = new ArrayList<>();
        List<Expression> conditions = asConditions(new NameExpr(new SimpleName(variableName)));
        for (Expression condition : conditions) {
            stringConditions.add(condition.toString());
        }
        return stringConditions;
    }

    public int numAbstractValues() {
        int numValues = 0;
        for (PuncturedIntervals.Interval interval : intervals) {
            numValues += interval.numAbstractValues();
        }
        return numValues;
    }

    public static class Interval {
        Long lowerBound;
        Long upperBound;

        /**
         * A sorted list of punctures
         */
        List<Long> punctures;

        public Interval(Long lowerBound, Long upperBound, List<Long> punctures) {
            this.lowerBound = lowerBound;
            this.upperBound = upperBound;
            this.punctures = new ArrayList<>();
            puncture(punctures);
        }

        public int numAbstractValues() {
            if (lowerBound.equals(upperBound)) {
                return 1;
            }
            if (punctures.isEmpty()) {
                return 1;
            }
            if (upperBound - lowerBound + 1 == punctures.size()) {
                return punctures.size();
            }
            return 1 + punctures.size();
        }

        private Expression and(Expression maybeNull, Expression cond2) {
            if (maybeNull == null) {
                return cond2;
            }
            return new BinaryExpr(maybeNull, cond2, BinaryExpr.Operator.AND);
        }

        private Expression lb(Expression boundExpr, Long bound) {
            return new BinaryExpr(boundExpr, new LongLiteralExpr(bound.toString()),
                    BinaryExpr.Operator.GREATER_EQUALS);
        }

        private Expression ub(Expression boundExpr, Long bound) {
            return new BinaryExpr(boundExpr, new LongLiteralExpr(bound.toString()), BinaryExpr.Operator.LESS_EQUALS);
        }

        private BinaryExpr ne(Expression expr, Long puncture) {
            return new BinaryExpr(expr, new LongLiteralExpr(puncture.toString()), BinaryExpr.Operator.NOT_EQUALS);
        }

        private BinaryExpr eq(Expression expr, Long puncture) {
            return new BinaryExpr(expr, new LongLiteralExpr(puncture.toString()), BinaryExpr.Operator.EQUALS);
        }

        /**
         * Make a condition that the variable is within the bounds and is not
         * one of the puncture points. If all points within this interval are punctured,
         * return null
         * 
         * @param expr the expr to check the condition against
         * @return the condition, if it exists, or else null
         */
        private Expression makeIntervalCondition(Expression expr) {
            Expression cond = null;
            if (lowerBound.equals(upperBound)) {
                return eq(expr, lowerBound);
            } else if (lowerBound + punctures.size() < upperBound) {
                if (lowerBound != Long.MIN_VALUE) {
                    cond = and(cond, lb(expr, lowerBound));
                }
                if (upperBound != Long.MAX_VALUE) {
                    cond = and(cond, ub(expr, upperBound));
                }
                // Now remove puncture points from this interval
                for (Long puncture : punctures) {
                    cond = and(cond, ne(expr, puncture));
                }
                return cond;
            }
            // Now, add puncture points
            for (Long puncture : punctures) {
                cond = and(cond, eq(expr, puncture));
            }

            return cond;
        }

        public List<Expression> asConditions(Expression expr) {
            List<Expression> conditions = new ArrayList<>();
            // The general case
            //
            // First, get the "full interval" condition: that is, get a condition
            // that says "expr is within the bounds of this interval and is
            // not a puncture point".
            Expression cond = makeIntervalCondition(expr);
            if (cond != null) {
                conditions.add(cond);
            }
            // Now add the individual puncture points. Skip this for singleton
            // intervals (lowerBound == upperBound)
            if (!lowerBound.equals(upperBound)) {
                for (Long puncture : punctures) {
                    conditions.add(eq(expr, puncture));
                }
            }
            return conditions;
        }

        public Interval(Long lowerBound, Long upperBound) {
            this(lowerBound, upperBound, (List<Long>) null);
        }

        public Interval(Long lowerBound, Long upperBound, Long... punctures) {
            this(lowerBound, upperBound);
            for (Long point : punctures) {
                puncture(point);
            }
        }

        public boolean puncture(Long point) {
            if (point == null) {
                throw new IllegalArgumentException("Puncture cannot be null");
            }
            if (point < this.lowerBound || point > this.upperBound) {
                return false;
            }

            for (int i = 0; i < this.punctures.size(); i++) {
                if (point.equals(this.punctures.get(i))) {
                    return false;
                }
                if (point > this.punctures.get(i)) {
                    this.punctures.add(i, point);
                    return true;
                }
            }
            this.punctures.add(point);
            return true;
        }

        public void puncture(List<Long> points) {
            if (points == null)
                return;
            for (Long point : points) {
                this.puncture(point);
            }
        }

        public List<PuncturedIntervals.Interval> splitAt(Long point) {
            if (point == null) {
                throw new IllegalArgumentException("Point cannot be null");
            }
            List<PuncturedIntervals.Interval> intervals = new ArrayList<>();

            if (point < this.lowerBound || point > this.upperBound) {
                intervals.add(this);
            } else if (point.equals(lowerBound) && point.equals(upperBound)) {
                intervals.add(new Interval(this.lowerBound, this.upperBound));
            } else {
                if (point > this.lowerBound) {
                    intervals.add(new Interval(this.lowerBound, point - 1, this.punctures));
                }
                intervals.add(new Interval(point, point));
                if (point < this.upperBound) {
                    intervals.add(new Interval(point + 1, this.upperBound, this.punctures));
                }
            }
            return intervals;
        }

        public boolean contains(Long point) {
            if (point == null) {
                throw new IllegalArgumentException("Point cannot be null");
            }
            return point >= this.lowerBound && point <= this.upperBound && !this.punctures.contains(point);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((lowerBound == null) ? 0 : lowerBound.hashCode());
            result = prime * result + ((upperBound == null) ? 0 : upperBound.hashCode());
            result = prime * result + ((punctures == null) ? 0 : punctures.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            PuncturedIntervals.Interval other = (PuncturedIntervals.Interval) obj;
            if (lowerBound == null) {
                if (other.lowerBound != null)
                    return false;
            } else if (!lowerBound.equals(other.lowerBound))
                return false;
            if (upperBound == null) {
                if (other.upperBound != null)
                    return false;
            } else if (!upperBound.equals(other.upperBound))
                return false;
            if (punctures == null) {
                if (other.punctures != null)
                    return false;
            } else if (!punctures.equals(other.punctures))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return "Interval [lowerBound=" + lowerBound + ", upperBound=" + upperBound + ", punctures=" + punctures
                    + "]";
        }

    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((intervals == null) ? 0 : intervals.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PuncturedIntervals other = (PuncturedIntervals) obj;
        if (intervals == null) {
            if (other.intervals != null)
                return false;
        } else if (!intervals.equals(other.intervals))
            return false;
        return true;
    }

    public String pretty(String name) {
        StringBuilder sb = new StringBuilder();
        for (PuncturedIntervals.Interval interval : intervals) {
            List<Expression> conditions = interval.asConditions(new NameExpr(name));
            for (Expression condition : conditions) {
                sb.append(condition.toString());
                sb.append(", ");
            }
        }
        return sb.toString();
    }
}