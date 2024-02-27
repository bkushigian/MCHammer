package org.mutation_testing.state;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.resolution.types.ResolvedType;

class StringStoreState extends StoreState {

    /**
     * Directly compared values. These are used similarly to a punctured
     * interval. These are from `.equals()` calls, not reference equality
     * checks.
     */
    Set<StringLiteralExpr> comparedValues;

    public void compareValue(StringLiteralExpr s) {
        comparedValues.add(s);
    }

    public StringStoreState(ResolvedType type) {
        super(type);
        comparedValues = new HashSet<>();
    }

    Expression eq(Expression e, StringLiteralExpr s) {
        return new BinaryExpr(e.clone(), s.clone(), BinaryExpr.Operator.EQUALS);
    }

    Expression ne(Expression e, StringLiteralExpr s) {
        return new BinaryExpr(e.clone(), s.clone(), BinaryExpr.Operator.NOT_EQUALS);
    }

    Expression and(List<Expression> es) {
        if (es.size() == 1) {
            return es.get(0).clone();
        }
        Expression e = es.get(0).clone();
        for (int i = 1; i < es.size(); i++) {
            e = new BinaryExpr(e, es.get(i).clone(), BinaryExpr.Operator.AND);
        }
        return e;
    }

    List<Expression> getTypeSpecificConditions(Expression expr) {
        List<Expression> cs = new ArrayList<>();
        if (!comparedValues.isEmpty()) {
            List<Expression> negated = new ArrayList<>();

            for (StringLiteralExpr s : comparedValues) {
                cs.add(eq(expr, s));
                negated.add(ne(expr, s));
            }
            cs.add(and(negated));

        }
        return cs;
    }

    @Override
    String pretty(String expr) {
        throw new UnsupportedOperationException("Unimplemented method 'pretty'");
    }
}