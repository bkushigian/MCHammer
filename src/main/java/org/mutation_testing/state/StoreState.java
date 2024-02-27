package org.mutation_testing.state;

import java.util.ArrayList;
import java.util.List;

import org.mutation_testing.predicates.MethodCallPredicate;

import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.UnaryExpr;
import com.github.javaparser.resolution.types.ResolvedType;

/**
 * This represents the states an expression can have
 */
abstract class StoreState {
    /**
     * Arbitrary predicates, invoked as method calls. E.g., `s.startsWith("foo")`
     */
    List<MethodCallPredicate> methodPredicates;

    /**
     * Get a list of boolean expressions that that determine each of the
     * abstract values in the store state.
     * 
     * @param name
     * @return
     */
    abstract List<Expression> getTypeSpecificConditions(Expression name);

    public List<Expression> getConditions(Expression name) {
        List<Expression> conditions = new ArrayList<>();
        conditions.addAll(getTypeSpecificConditions(name));
        conditions.addAll(methodConditions());
        return conditions;
    }

    List<Expression> methodConditions() {
        List<Expression> conditions = new ArrayList<>();
        for (MethodCallPredicate p : methodPredicates) {
            Expression call = p.getMethodCall().clone();
            conditions.add(call.clone());
            conditions.add(new UnaryExpr(call, UnaryExpr.Operator.LOGICAL_COMPLEMENT));
        }
        return conditions;
    }

    public void addPredicate(MethodCallPredicate p) {
        methodPredicates.add(p);
    }

    /**
     * Pretty print the abstract value of the given expression
     * 
     * @param expr
     * @return
     */
    abstract String pretty(String expr);

    ResolvedType type;

    StoreState(ResolvedType type) {
        this.type = type;
        methodPredicates = new ArrayList<>();
    }

}