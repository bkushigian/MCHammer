package org.mutation_testing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mutation_testing.relation.NameLiteralRelation;
import org.mutation_testing.relation.Relation;

import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.BinaryExpr.Operator;
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

    NameVisitor nv = new NameVisitor();

    public Store() {
        fieldStore = new HashMap<>();
        localStore = new HashMap<>();
        miscStore = new HashMap<>();
    }

    static final int UNKNOWN = 0;
    static final int ORDERED = 1;
    static final int UNORDERED = 2;

    private int relOpType(Operator op) {
        switch (op) {
            case GREATER:
            case GREATER_EQUALS:
            case LESS:
            case LESS_EQUALS:
                return ORDERED;
            case EQUALS:
            case NOT_EQUALS:
                return UNORDERED;
            default:
                throw new IllegalArgumentException("Unsupported relational operator: " + op.toString());
        }
    }

    StoreState addToStore(NameLiteralRelation relation) {
        String ident = relation.getName().getName().getIdentifier();
        LiteralExpr lit = relation.getLiteral();
        StoreState state = localStore.get(ident);

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

        if (state == null) {
            state = new StoreState(type);
            localStore.put(ident, state);
        }

        if (state.type != type) {
            throw new IllegalArgumentException("Type mismatch for variable " + ident + ": " + state.type + " != " + type);
        }

        if (relation.isOrdered()) {
            state.intervals.puncture(value);
        } else {
            state.intervals.splitAt(value);
        }

        return state;
    }

    void processRelation(Relation relation) {
        if (relation.isNameLiteralRelation()) {
            addToStore(relation.asNameLiteralRelation());
        } else {
            throw new IllegalArgumentException("Unsupported relation type: " + relation.getClass().getName());
        }
        
    }
}
