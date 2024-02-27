package org.mutation_testing.predicates;

import com.github.javaparser.ast.expr.NameExpr;

public class NamePredicate extends Predicate {
    private NameExpr name;

    public NamePredicate(NameExpr name) {
        this.name = name;
    }

    public NameExpr getName() {
        return name;
    }

    @Override
    public NamePredicate asNamePredicate() {
        return this;
    }

    @Override
    public boolean isNamePredicate() {
        return true;
    }
    
}
