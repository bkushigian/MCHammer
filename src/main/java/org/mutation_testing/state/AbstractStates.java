package org.mutation_testing.state;

import java.util.List;

import com.github.javaparser.ast.expr.Expression;

public interface AbstractStates {

    List<String> asStringConditions(String expr);

    /**
     * Emit a list of boolean expressions that determine if the given expression
     * is in a given state.
     * 
     * @param expr
     * @return
     */
    List<Expression> asConditions(Expression expr);

}
