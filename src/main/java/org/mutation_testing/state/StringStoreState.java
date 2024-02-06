package org.mutation_testing.state;

import java.util.List;

import org.mutation_testing.NotImplementedException;

import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.resolution.types.ResolvedType;

class StringStoreState extends StoreState {

    /**
     * Directly compared values. These are used similarly to a punctured
     * interval. These are from `.equals()` calls, not reference equality
     * checks.
     * 
     * TODO: How to hande reference equality checks?
     */
    List<String> values;

    /**
     * Arbitrary predicates, invoked as method calls. E.g., `s.startsWith("foo")`
     */
    List<MethodCallExpr> predicates;

    public StringStoreState(ResolvedType type) {
        super(type);
    }
    
    List<Expression> asConditions(Expression expr) {
        throw new NotImplementedException("StringStoreState.asConditions() not implemented");
    }

    @Override
    String pretty(String expr) {
        throw new UnsupportedOperationException("Unimplemented method 'pretty'");
    }
}