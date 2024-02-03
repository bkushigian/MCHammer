package org.mutation_testing.relation;

import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.LiteralExpr;

public class LiteralLiteralRelation extends Relation {

    LiteralExpr literal1;
    LiteralExpr literal2;

    public LiteralLiteralRelation(BinaryExpr relation, LiteralExpr left, LiteralExpr right) {
        super(relation, RelationType.LITERAL_LITERAL);
        this.literal1 = left;
        this.literal2 = right;
    }

    public LiteralExpr getLiteral1() {
        return literal1;
    }

    public LiteralExpr getLiteral2() {
        return literal2;
    }

    @Override
    public LiteralLiteralRelation asLiteralLiteralRelation() {
        return this;
    }

    @Override
    public boolean isLiteralLiteralRelation() {
        return true;
    }
    
}
