package org.mutation_testing.state;

import java.util.List;

import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.resolution.types.ResolvedType;

/**
 * This represents the states an expression can have
 */
abstract class StoreState {

    /**
     * Get a list of boolean expressions that that determine each of the
     * abstract values in the store state.
     * 
     * @param name
     * @return
     */
    abstract List<Expression> asConditions(Expression name);

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
    }

}