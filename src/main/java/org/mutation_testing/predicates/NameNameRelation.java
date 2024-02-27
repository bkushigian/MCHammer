package org.mutation_testing.predicates;

import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.NameExpr;

public class NameNameRelation extends Relation {
    NameExpr name1;
    NameExpr name2;
    NameNameRelation(BinaryExpr relation, NameExpr left, NameExpr right) {
        super(relation, RelationType.NAME_NAME);
        this.name1 = left;
        this.name2 = right;
    }

    public NameExpr getName1() {
        return name1;
    }

    public NameExpr getName2() {
        return name2;
    }

    @Override
    public NameNameRelation asNameNameRelation() {
        return this;
    }

    @Override
    public boolean isNameNameRelation() {
        return true;
    }
}
