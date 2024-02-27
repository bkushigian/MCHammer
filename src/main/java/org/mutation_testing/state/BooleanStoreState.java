package org.mutation_testing.state;

import java.util.Arrays;
import java.util.List;

import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.UnaryExpr;
import com.github.javaparser.resolution.types.ResolvedPrimitiveType;

/**
 * A boolean state store can only have two abstract values, true or false, and
 * both should always be checked.
 */
public class BooleanStoreState extends StoreState {

    BooleanStoreState() {
        super(ResolvedPrimitiveType.BOOLEAN);
    }

    @Override
    List<Expression> getTypeSpecificConditions(Expression name) {
        return Arrays.asList(name, new UnaryExpr(name, UnaryExpr.Operator.LOGICAL_COMPLEMENT));
    }

    @Override
    String pretty(String expr) {
        return "[" + expr + ", !" + expr + "]";
    }
    
}
