package org.mutation_testing.predicates;

import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.NameExpr;

public class ExprNameRelation extends Relation {
    NameExpr name;
    Expression expr;

    public ExprNameRelation(BinaryExpr relation, NameExpr name, Expression right) {
        super(relation, RelationType.NAME_NAME);
        this.name = name;
        this.expr = right;
    }

    @Override
    public boolean isExprNameRelation() {
        return true;
    }

    @Override
    public ExprNameRelation asExprNameRelation() {
        return this;
    }

    public NameExpr getName() {
        return name;
    }

    public Expression getExpr() {
        return expr;
    }
    
}
