package org.mutation_testing.state;

import java.util.ArrayList;
import java.util.List;

import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.resolution.types.ResolvedType;

class IntStoreState extends StoreState {
    final PuncturedIntervals intervals;
    final List<BinaryExpr> additionalRelations;

    public IntStoreState(ResolvedType type, PuncturedIntervals intervals, List<BinaryExpr> additionalRelations) {
        super(type);
        this.intervals = intervals;
        this.additionalRelations = additionalRelations;
    }

    public IntStoreState(ResolvedType type) {
        super(type);
        this.intervals = new PuncturedIntervals();
        this.additionalRelations = new ArrayList<>();
    }

    @Override
    List<Expression> asConditions(Expression expr) {
        List<Expression> conditions = new ArrayList<>();
        conditions.addAll(intervals.asConditions(expr));
        for (BinaryExpr relation : additionalRelations) {
            conditions.add(relation);
        }
        return conditions;
    }

    @Override
    String pretty(String expr) {
        return intervals.pretty(expr);
    }
}