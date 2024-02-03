package org.mutation_testing.relation;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.Expression;

public class ExprExprRelation extends Relation {

    Expression expr1;
    Expression expr2;

    public ExprExprRelation(BinaryExpr relation, Expression expr1, Expression expr2) {
        super(relation, RelationType.EXPR_EXPR);
        this.expr1 = expr1;
        this.expr2 = expr2;
    }

    public Expression getExpr1() {
        return expr1;
    }

    public Expression getExpr2() {
        return expr2;
    }

    @Override
    public ExprExprRelation asExprExprRelation() {
        return this;
    }

    @Override
    public boolean isExprExprRelation() {
        return true;
    }
}
