package org.mutation_testing;

import com.github.javaparser.ast.DataKey;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.resolution.types.ResolvedType;

public class ExpressionData {
    public static final DataKey<ExpressionData> DATA_KEY = new DataKey<ExpressionData>() { };

    /**
     * Resolved type information
     */
    public final ResolvedType type;

    /**
     * Reference to the original node where this was computed
     */
    public final Node originalNode;


    private ExpressionData(ResolvedType type, Node originalNode) {
        this.type = type;
        this.originalNode = originalNode;
    }


    public static ExpressionData applyToExpression(Expression expr) {
        if (expr.containsData(DATA_KEY)) {
            throw new IllegalStateException("Node already contains NodeData: " + expr);
        }

        ResolvedType t = expr.calculateResolvedType();

        ExpressionData data = new ExpressionData(t, expr);
        expr.setData(DATA_KEY, data);
        return data;
    }

    @Override
    public String toString() {
        return "ExpressionData { type: " + type + ", originalNode: " + originalNode + "}";
    }
}
