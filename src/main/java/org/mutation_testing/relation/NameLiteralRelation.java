package org.mutation_testing.relation;

import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.LiteralExpr;
import com.github.javaparser.ast.expr.NameExpr;

public class NameLiteralRelation extends Relation {
    NameExpr name;
    LiteralExpr literal;
    NameLiteralRelation(BinaryExpr relation, NameExpr name, LiteralExpr literal) {
        super(relation, RelationType.NAME_LITERAL);
        this.name = name;
        this.literal = literal;
    }

    public NameExpr getName() {
        return name;
    }

    public LiteralExpr getLiteral() {
        return literal;
    }

    @Override
    public NameLiteralRelation asNameLiteralRelation() {
        return this;
    }

    @Override
    public boolean isNameLiteralRelation() {
        return true;
    }
}
