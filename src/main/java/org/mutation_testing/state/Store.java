package org.mutation_testing.state;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.mutation_testing.NotImplementedException;
import org.mutation_testing.relation.ExprExprRelation;
import org.mutation_testing.relation.ExprLiteralRelation;
import org.mutation_testing.relation.ExprNameRelation;
import org.mutation_testing.relation.LiteralLiteralRelation;
import org.mutation_testing.relation.NameLiteralRelation;
import org.mutation_testing.relation.NameNameRelation;
import org.mutation_testing.relation.Relation;

import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.LiteralExpr;
import com.github.javaparser.ast.expr.NameExpr;

/**
 * This stores abstract values for parameters, fields, local variables, etc
 */
public class Store {
    /**
     * This represents the state an expression can have
     */
    static class StoreState {
        final PrimitiveType type;
        final PuncturedIntervals intervals;
        final List<BinaryExpr> additionalRelations;

        public StoreState(PrimitiveType type, PuncturedIntervals intervals, List<BinaryExpr> additionalRelations) {
            this.type = type;
            this.intervals = intervals;
            this.additionalRelations = additionalRelations;
        }

        public StoreState(PrimitiveType type) {
            this.type = type;
            this.intervals = new PuncturedIntervals();
            this.additionalRelations = new ArrayList<>();
        }
    }

    /**
     * Map field names to a store state
     */
    Map<String, StoreState> fieldStore;

    /**
     * Map local variable names to a store state
     */
    Map<String, StoreState> localStore;

    /**
     * Each list of expressions should be boolean expressions such that
     * precisely one is true at any given time. This is not checked and must be
     * maintained by the developer.
     */
    Map<Expression, StoreState> miscStore;

    public Store() {
        fieldStore = new HashMap<>();
        localStore = new HashMap<>();
        miscStore = new HashMap<>();
    }

    public Store(List<Relation> relations) {
        this();
        for (Relation relation : relations) {
            addRelation(relation);
        }
    }

    private void productHelper(List<List<Expression>> conds,
            int condIndex,
            List<Expression> builtSoFar,
            List<Expression> result) {
        List<Expression> current = conds.get(condIndex);

    }

    List<Expression> productOfConditions(List<List<Expression>> conditions) {
        if (conditions.isEmpty()) {
            return new ArrayList<>();
        }

        int numConditions = conditions.size();
        int[] indices = new int[numConditions];
        int[] sizes = new int[numConditions];
        int totalSize = 1;
        for (int i = 0; i < numConditions; i++) {
            sizes[i] = conditions.get(i).size();
            totalSize *= sizes[i];
        }

        List<Expression> product = new ArrayList<>(totalSize);
        for (int i = 0; i < totalSize; i++) {
            List<Expression> condition = new ArrayList<>(numConditions);
            for (int j = 0; j < numConditions; j++) {
                condition.add(conditions.get(j).get(indices[j]));
            }
            product.add(BinaryExpr.and(condition));
            for (int j = 0; j < numConditions; j++) {
                indices[j] += 1;
                if (indices[j] < sizes[j]) {
                    break;
                }
                indices[j] = 0;
            }
        }

        return product;
    }

    List<Expression> asProductConditions() {
        if (!fieldStore.isEmpty()) {
            throw new NotImplementedException("asProductConditions() not implemented for fields");
        }
        if (!miscStore.isEmpty()) {
            throw new NotImplementedException("asProductConditions() not implemented for miscStore");
        }

        int numLocalVars = localStore.size();
        int abstractValueProductSize = 1;
        System.out.println("Number of Local Variables: " + numLocalVars);

        List<List<Expression>> conditionsToProduct = new ArrayList<>();

        for (Map.Entry<String, StoreState> e : localStore.entrySet()) {
            String ident = e.getKey();
            NameExpr name = new NameExpr(ident);
            int numAbstractValues = e.getValue().intervals.numAbstractValues();
            abstractValueProductSize *= numAbstractValues;
            System.out.println("Number of Abstract Values for " + e.getKey() + ": " + numAbstractValues);
            conditionsToProduct.add(e.getValue().intervals.asConditions(name));
        }

        System.out.println("Abstract Value Product Size: " + abstractValueProductSize);

        List<Expression> conditions = new ArrayList<>();

        Stack<Integer> stack = new Stack<>();
        while (true) {

        }

        return conditions;
    }

    static final int UNKNOWN = 0;
    static final int ORDERED = 1;
    static final int UNORDERED = 2;

    StoreState addToStore(NameLiteralRelation relation) {
        String ident = relation.getName().getName().getIdentifier();
        LiteralExpr lit = relation.getLiteral();

        Long value;
        PrimitiveType type;

        if (lit.isIntegerLiteralExpr()) {
            type = PrimitiveType.intType();
            value = Long.parseLong(lit.asIntegerLiteralExpr().getValue());
        } else if (lit.isCharLiteralExpr()) {
            type = PrimitiveType.charType();
            value = (long) lit.asCharLiteralExpr().getValue().charAt(0);
        } else if (lit.isLongLiteralExpr()) {
            type = PrimitiveType.longType();
            value = Long.parseLong(lit.asLongLiteralExpr().getValue());
        } else {
            throw new IllegalArgumentException("Unsupported literal type");
        }

        StoreState state = localStore.computeIfAbsent(ident, k -> new StoreState(type));

        if (state.type != type) {
            throw new IllegalArgumentException(
                    "Type mismatch for variable " + ident + ": " + state.type + " != " + type);
        }

        if (relation.isOrdered()) {
            state.intervals.puncture(value);
        } else {
            state.intervals.splitAt(value);
        }

        return state;
    }

    void addToStore(NameNameRelation relation) {
        throw new NotImplementedException("addToStore(NameNameRelation) not implemented");
    }

    void addToStore(ExprNameRelation relation) {
        throw new NotImplementedException("addToStore(ExprNameRelation) not implemented");
    }

    void addToStore(LiteralLiteralRelation relation) {
        // Do nothing
    }

    void addToStore(ExprLiteralRelation relation) {
        throw new NotImplementedException("addToStore(ExprLiteralRelation) not implemented");
    }

    void addToStore(ExprExprRelation relation) {
        throw new NotImplementedException("addToStore(ExprExprRelation) not implemented");
    }

    void addRelation(Relation relation) {
        if (relation.isNameLiteralRelation()) {
            addToStore(relation.asNameLiteralRelation());
        } else if (relation.isNameNameRelation()) {
            addToStore(relation.asNameNameRelation());
        } else if (relation.isExprNameRelation()) {
            addToStore(relation.asExprNameRelation());
        } else if (relation.isLiteralLiteralRelation()) {
            addToStore(relation.asLiteralLiteralRelation());
        } else if (relation.isExprLiteralRelation()) {
            addToStore(relation.asExprLiteralRelation());
        } else if (relation.isExprExprRelation()) {
            addToStore(relation.asExprExprRelation());
        } else {
            throw new IllegalArgumentException("Unknown relation type: " + relation);
        }
    }
}
