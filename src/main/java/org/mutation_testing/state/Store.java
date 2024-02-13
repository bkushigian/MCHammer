package org.mutation_testing.state;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mutation_testing.NotImplementedException;
import org.mutation_testing.predicates.ExprExprRelation;
import org.mutation_testing.predicates.ExprLiteralRelation;
import org.mutation_testing.predicates.ExprNameRelation;
import org.mutation_testing.predicates.LiteralLiteralRelation;
import org.mutation_testing.predicates.MethodCallPredicate;
import org.mutation_testing.predicates.NameLiteralRelation;
import org.mutation_testing.predicates.NameNameRelation;
import org.mutation_testing.predicates.NamePredicate;
import org.mutation_testing.predicates.Predicate;
import org.mutation_testing.predicates.Relation;

import com.github.javaparser.resolution.types.ResolvedPrimitiveType;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.LiteralExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;

/**
 * This stores abstract values for parameters, fields, local variables, etc
 */
public class Store {

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

    List<MethodCallPredicate> unscopedMethodCalls;

    public Store() {
        fieldStore = new HashMap<>();
        localStore = new HashMap<>();
        miscStore = new HashMap<>();
        unscopedMethodCalls = new ArrayList<>();
    }

    public Store(List<Predicate> predicates) {
        this();
        for (Predicate predicate : predicates) {
            addPredicate(predicate);
        }
    }

    public List<Expression> getProductConditions() {
        return StateProduct.getProductConditions(localStore);
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

    /**
     * A helper function that adds a primitive to the store
     * 
     * @param relation
     * @param typ
     * @return
     */
    private StoreState addPrimitiveType(NameLiteralRelation relation, ResolvedPrimitiveType typ) {
        NameExpr nameExpr = relation.getName();
        LiteralExpr lit = relation.getLiteral();

        switch (typ.name()) {
            case "INT":
            case "LONG":
            case "CHAR":
            case "SHORT":
                IntStoreState state = (IntStoreState) localStore.computeIfAbsent(nameExpr.getNameAsString(),
                        k -> new IntStoreState(typ));
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

    /**
     * A helper function that adds a reference to the store
     * 
     * @param relation
     * @param typ
     * @return
     */
    private StoreState addReferenceType(NameLiteralRelation relation, ResolvedReferenceType typ) {
        NameExpr nameExpr = relation.getName();

        if ("java.lang.String".equals(typ.getQualifiedName())) {
            StringStoreState state = (StringStoreState) localStore.computeIfAbsent(nameExpr.getNameAsString(),
                    k -> new StringStoreState(typ));
            if (relation.getLiteral().isStringLiteralExpr()) {
                state.compareValue(relation.getLiteral().asStringLiteralExpr());
            }
            return state;
        } else {
            throw new IllegalArgumentException("Unsupported reference type: " + typ.getQualifiedName());
        }
    }

    StoreState addToStore(NameLiteralRelation relation) {
        ResolvedType resolvedType = relation.getName().resolve().getType();

        if (resolvedType.isPrimitive()) {
            return addPrimitiveType(relation, resolvedType.asPrimitive());
        } else if (resolvedType.isReference()) {
            return addReferenceType(relation, resolvedType.asReferenceType());
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

    void addMethodCallPredicate(MethodCallPredicate predicate) {
        Expression scope = predicate.getScope().orElse(null);
        if (scope == null) {
            System.err.println("Unscoped method call: " + predicate.getMethodCall());
            unscopedMethodCalls.add(predicate);
            return;
        }

        System.out.println(predicate.getMethodCall());
        System.out.println(scope);
        System.out.println(scope.getClass());

        if (scope.isNameExpr()) {
            NameExpr nameScope = scope.asNameExpr();
            StoreState state = localStore.get(nameScope.getNameAsString());
            if (state == null) {
                state = getStoreStateForType(nameScope.resolve().getType());
                localStore.put(nameScope.getNameAsString(), state);
            }
            state.addPredicate(predicate);

        } else {
            throw new NotImplementedException("Method call on non-name not implemented");
        }
    }

    void addNamePredicate(NamePredicate predicate) {
        throw new NotImplementedException("addNamePredicate() not implemented");
    }

    void addPredicate(Predicate predicate) {
        if (predicate.isRelation()) {
            addRelation(predicate.asRelation());
        } else if (predicate.isMethodCallPredicate()) {
            addMethodCallPredicate(predicate.asMethodCallPredicate());
        } else if (predicate.isNamePredicate()) {
            addNamePredicate(predicate.asNamePredicate());
        } else {
            throw new NotImplementedException("addPredicate() not implemented for non-relations");
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

    /**
     * Get a store state for a resolved type t
     * 
     * @param t
     * @return
     */
    StoreState getStoreStateForType(ResolvedType t) {
        if (t.isPrimitive()) {
            ResolvedPrimitiveType pt = t.asPrimitive();
            switch (pt) {
                case BOOLEAN:
                    return new BooleanStoreState();
                case BYTE:
                case CHAR:
                case SHORT:
                case INT:
                case LONG:
                    return new IntStoreState(pt);
                case FLOAT:
                case DOUBLE:
                    throw new NotImplementedException("Floating point types not implemented");
                default:
                    throw new IllegalArgumentException("Unsupported primitive type");
            }
        } else if (t.isReferenceType()) {
            ResolvedReferenceType rt = t.asReferenceType();
            switch (rt.getQualifiedName()) {
                case "java.lang.String":
                    return new StringStoreState(rt);
                case "java.lang.Boolean":
                    return new BooleanStoreState();
                case "java.lang.Integer":
                case "java.lang.Long":
                case "java.lang.Short":
                case "java.lang.Byte":
                case "java.lang.Character":
                    return new IntStoreState(rt);
                default:
                    return new ObjectStoreState(rt);
            }
        } else {
            throw new IllegalArgumentException("Unsupported type");
        }

    }
}
