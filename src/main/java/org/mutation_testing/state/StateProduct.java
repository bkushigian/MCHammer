package org.mutation_testing.state;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.EnclosedExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.NameExpr;

/**
 * Compute the product of states
 */
public class StateProduct {
    private StateProduct() {
        throw new UnsupportedOperationException("This class should not be instantiated");
    }

    public static List<Expression> getProductConditions(Map<String, StoreState> localStore) {

        List<List<Expression>> conditionsToProduct = new ArrayList<>();

        for (Map.Entry<String, StoreState> e : localStore.entrySet()) {
            String ident = e.getKey();
            NameExpr name = new NameExpr(ident);
            conditionsToProduct.add(e.getValue().getConditions(name));
        }

        return conditionProductHelper(conditionsToProduct);
    }

    private static void incrementIndices(int[] indices, int[] sizes) {
        for (int i = 0; i < indices.length; i++) {
            indices[i] += 1;
            if (indices[i] < sizes[i]) {
                break;
            }
            indices[i] = 0;
        }
    }

    private static List<Expression> conditionProductHelper(List<List<Expression>> abstractValueConditions) {
        int[] indices = new int[abstractValueConditions.size()]; // Track the current index for each list of conditions
        int[] sizes = new int[abstractValueConditions.size()]; // Track the size of each list of conditions
        int totalSize = 1;

        for (int i = 0; i < abstractValueConditions.size(); i++) {
            sizes[i] = abstractValueConditions.get(i).size();
            totalSize *= sizes[i];
        }

        List<Expression> product = new ArrayList<>();

        for (int i = 0; i < totalSize; i++) {
            Expression thisCondition = null;

            for (int j = 0; j < abstractValueConditions.size(); j++) {
                thisCondition = and(thisCondition, abstractValueConditions.get(j).get(indices[j]));
            }
            if (thisCondition != null) {
                product.add(thisCondition);
            }
            incrementIndices(indices, sizes);
        }

        return product;
    }

    private static Expression enclose(Expression expr) {
        return new EnclosedExpr(expr);
    }

    private static Expression and(Expression maybeNull, Expression cond2) {
        if (maybeNull == null) {
            return cond2;
        }

        // Enclose other expressions that have lower precedence. This includes:
        // - OR binary operators
        // - Ternary conditional operators
        // - Assignment operators
        if (maybeNull.isBinaryExpr() && maybeNull.asBinaryExpr().getOperator() == BinaryExpr.Operator.OR
                || maybeNull.isConditionalExpr()
                || maybeNull.isAssignExpr()) {
            maybeNull = enclose(maybeNull);
        }
        return new BinaryExpr(maybeNull, cond2, BinaryExpr.Operator.AND);
    }
}
