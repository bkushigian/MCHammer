package org.mutation_testing.state;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mutation_testing.NotImplementedException;
import org.mutation_testing.relation.ExprExprRelation;
import org.mutation_testing.relation.ExprLiteralRelation;
import org.mutation_testing.relation.ExprNameRelation;
import org.mutation_testing.relation.LiteralLiteralRelation;
import org.mutation_testing.relation.NameLiteralRelation;
import org.mutation_testing.relation.NameNameRelation;
import org.mutation_testing.relation.Relation;

import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.EnclosedExpr;
import com.github.javaparser.resolution.types.ResolvedPrimitiveType;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;
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
    abstract static class StoreState {
        abstract List<Expression> asConditions(NameExpr name);
        abstract String pretty(String expr);

        ResolvedType type;

        StoreState(ResolvedType type) {
            this.type = type;
        }

    }

    static class IntStoreState extends StoreState {
        final PuncturedIntervals intervals;
        final List<BinaryExpr> additionalRelations;

        public IntStoreState(ResolvedType type, PuncturedIntervals intervals, List<BinaryExpr> additionalRelations) {
            super(type);
            this.intervals = intervals;
            this.additionalRelations = additionalRelations;
        }

        public IntStoreState(ResolvedType type) {
            super(type);
            this.intervals = new PuncturedIntervals();
            this.additionalRelations = new ArrayList<>();
        }

        @Override
        List<Expression> asConditions(NameExpr name) {
            List<Expression> conditions = new ArrayList<>();
            conditions.addAll(intervals.asConditions(name));
            for (BinaryExpr relation : additionalRelations) {
                conditions.add(relation);
            }
            return conditions;
        }

        @Override
        String pretty(String expr) {
            return intervals.pretty(expr);
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

    private Expression enclose(Expression expr) {
        return new EnclosedExpr(expr);
    }

    private Expression and(Expression maybeNull, Expression cond2) {
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

    private void incrementIndices(int[] indices, int[] sizes) {
        for (int i = 0; i < indices.length; i++) {
            indices[i] += 1;
            if (indices[i] < sizes[i]) {
                break;
            }
            indices[i] = 0;
        }
    }

    private List<Expression> conditionProductHelper(List<List<Expression>> abstractValueConditions) {
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
            product.add(thisCondition);
            incrementIndices(indices, sizes);
        }

        return product;
    }

    public List<Expression> getProductConditions() {
        if (!fieldStore.isEmpty()) {
            throw new NotImplementedException("asProductConditions() not implemented for fields");
        }
        if (!miscStore.isEmpty()) {
            throw new NotImplementedException("asProductConditions() not implemented for miscStore");
        }

        List<List<Expression>> conditionsToProduct = new ArrayList<>();

        for (Map.Entry<String, StoreState> e : localStore.entrySet()) {
            String ident = e.getKey();
            NameExpr name = new NameExpr(ident);
            conditionsToProduct.add(e.getValue().asConditions(name));
        }

        return conditionProductHelper(conditionsToProduct);
    }

    static final int UNKNOWN = 0;
    static final int ORDERED = 1;
    static final int UNORDERED = 2;


    Long getLongValueFromLiteral(LiteralExpr lit) {
        if (lit.isIntegerLiteralExpr()) {
            return Long.parseLong(lit.asIntegerLiteralExpr().getValue());
        } else if (lit.isCharLiteralExpr()) {
            return (long) lit.asCharLiteralExpr().getValue().charAt(0);
        } else if (lit.isLongLiteralExpr()) {
            return Long.parseLong(lit.asLongLiteralExpr().getValue());
        } else {
            throw new IllegalArgumentException("Unsupported literal type");
        }
    }
    StoreState addPrimitiveToStore(NameLiteralRelation relation, ResolvedPrimitiveType typ) {
        NameExpr nameExpr = relation.getName();
        LiteralExpr lit = relation.getLiteral();

        System.out.println("Type: " + typ.name());

        switch (typ.name()) {
            case "INT":
            case "LONG":
            case "CHAR":
            case "SHORT":
                IntStoreState state = (IntStoreState) localStore.computeIfAbsent(nameExpr.getNameAsString(), k -> new IntStoreState(typ));
                Long value = getLongValueFromLiteral(lit);
                if (relation.isOrdered()) {
                    state.intervals.splitAt(value);
                } else {
                    state.intervals.puncture(value);
                }
                return state;
            case "FLOAT":
            case "DOUBLE":
                throw new NotImplementedException("Floating point literals not implemented");
            case "BOOLEAN":
                throw new NotImplementedException("Boolean literals not implemented");
            default:
                throw new IllegalArgumentException("Unsupported primitive type");
        }
    }

    StoreState addReferenceToStore(NameLiteralRelation relation, ResolvedReferenceType typ) {
        NameExpr nameExpr = relation.getName();
        System.out.println("Type: " + typ.describe());
        StoreState state = localStore.computeIfAbsent(nameExpr.getNameAsString(), k -> new IntStoreState(typ));

        if ("java.lang.String".equals(typ.getQualifiedName())) {
            throw new NotImplementedException("String literals not implemented");
        } else if (true) {
            throw new IllegalArgumentException("Unsupported reference type: " + typ.getQualifiedName());
        }
        return state;
    }

    StoreState addToStore(NameLiteralRelation relation) {
        ResolvedType resolvedType = relation.getName().resolve().getType();

        if (resolvedType.isPrimitive()) {
            return addPrimitiveToStore(relation, resolvedType.asPrimitive());
        } else if (resolvedType.isReference()) {
            throw new NotImplementedException("Reference types not implemented");
        } else {
            throw new IllegalArgumentException("Unsupported type");
        }
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

    public String pretty() {
        StringBuilder sb = new StringBuilder();
        sb.append("Local variables:\n");
        for (Map.Entry<String, StoreState> e : localStore.entrySet()) {
            String name = e.getKey();
            StoreState state = e.getValue();
            sb.append(name).append(": ").append("{ ")
                    .append(state.pretty(name)).append(" }\n");
        }
        return sb.toString();
    }
}
