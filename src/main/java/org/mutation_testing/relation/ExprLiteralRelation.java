package org.mutation_testing.relation;

import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.LiteralExpr;

public class ExprLiteralRelation extends Relation {

    LiteralExpr literal;
    Expression expr;

    public ExprLiteralRelation(BinaryExpr relation, LiteralExpr literal, Expression expr) {
        super(relation, RelationType.LITERAL_LITERAL);
        this.literal = literal;
        this.expr = expr;
    }

    public LiteralExpr getLiteral() {
        return literal;
    }

    public Expression getExpr() {
        return expr;
    }

    @Override
    public ExprLiteralRelation asExprLiteralRelation() {
        return this;
    }

    @Override
    public boolean isExprLiteralRelation() {
        return true;
    }
    
}
