package org.mutation_testing.state;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import org.mutation_testing.predicates.MethodCallPredicate;

import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.resolution.types.ResolvedReferenceType;

public class ObjectStoreState extends StoreState {

    ObjectStoreState(ResolvedReferenceType type) {
        super(type);
    }

    @Override
    List<Expression> getTypeSpecificConditions(Expression name) {
        return new ArrayList<>();
    }

    @Override
    String pretty(String expr) {
        StringJoiner sj = new StringJoiner(", ", "[", "]");
        for (MethodCallPredicate p : methodPredicates) {
            sj.add(p.getMethodCall().toString());
        }
        return "ObjectStoreState{" +
                "methodPredicates=" + sj.toString() +
                ", type=" + type +
                '}';
    }
    
}
