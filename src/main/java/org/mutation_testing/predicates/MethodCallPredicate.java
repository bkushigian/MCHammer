package org.mutation_testing.predicates;

import java.util.Optional;

import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.resolution.types.ResolvedType;

public class MethodCallPredicate extends Predicate {

    private final MethodCallExpr methodCall;

    public MethodCallPredicate(MethodCallExpr methodCall) {
        this.methodCall = methodCall;
        ResolvedType tp = methodCall.resolve().getReturnType();

        // Ensure this is a boolean method
        if (!(tp.isPrimitive() && tp.asPrimitive().isBoolean())
                && !(tp.isReferenceType() && tp.asReferenceType().getQualifiedName().equals("java.lang.Boolean"))) {
            throw new IllegalArgumentException("Method call must return a boolean");
        }
    }

    @Override
    public MethodCallPredicate asMethodCallPredicate() {
        return this;
    }

    @Override
    public boolean isMethodCallPredicate() {
        return true;
    }

    public MethodCallExpr getMethodCall() {
        return methodCall;
    }

    public Optional<Expression> getScope() {
        return methodCall.getScope();
    }
}
