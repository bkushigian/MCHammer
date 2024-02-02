package org.mutation_testing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.BinaryExpr.Operator;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
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

    NameVisitor nv = new NameVisitor();

    public Store() {
        fieldStore = new HashMap<>();
        localStore = new HashMap<>();
        miscStore = new HashMap<>();
    }

    static final int UNKNOWN = 0;
    static final int ORDERED = 1;
    static final int UNORDERED = 2;

    StoreState addToStore(NameExpr name, LiteralExpr lit, Operator op) {
        String ident = name.getName().getIdentifier();
        StoreState state = localStore.get(ident);
        int opType;

        switch (op) {
            case GREATER:
            case GREATER_EQUALS:
            case LESS:
            case LESS_EQUALS:
                opType = ORDERED;
                break;
            case EQUALS:
            case NOT_EQUALS:
                opType = UNORDERED;
                break;
            default:
                throw new IllegalArgumentException("Unsupported operator");
        }
        Long value;
        PrimitiveType type;
        if (lit.isIntegerLiteralExpr()) {
            type = PrimitiveType.intType();
            value = Long.parseLong(lit.asIntegerLiteralExpr().getValue());
        } else if (lit.isCharLiteralExpr()) {
            type = PrimitiveType.charType();
            value = (long) lit.asCharLiteralExpr().getValue().charAt(0);
        } else {
            throw new IllegalArgumentException("Unsupported literal type");
        }
        if (state == null) {
            state = new StoreState(type);
            localStore.put(ident, state);
        }
        if (opType == ORDERED) {
            state.intervals.puncture(value);
        } else {
            state.intervals.splitAt(value);
        }
        return state;

    }

    void processNameLiteralRelation(BinaryExpr relation) {
        Expression left = relation.getLeft();
        Expression right = relation.getRight();
        Operator op = relation.getOperator();

        if (left.isLiteralExpr() && right.isNameExpr()) {
            addToStore(right.asNameExpr(), left.asLiteralExpr(), op);
        } else if (left.isNameExpr() && right.isLiteralExpr()) {
            addToStore(left.asNameExpr(), right.asLiteralExpr(), op);
        } else {
            throw new IllegalArgumentException("Can only handle relations comparing a literal and a name");
        }
    }
}
