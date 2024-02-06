package org.mutation_testing.predicates;

import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.Expression;

public abstract class Relation {
    protected boolean isOrdered;
    protected RelationType relationType;
    protected BinaryExpr relationNode;

    protected Relation(BinaryExpr expr, RelationType relationType) {
        if (!opIsRelational(expr.getOperator()))
            throw new IllegalArgumentException("Operator must be a relational operator: " + expr);
        this.relationNode = expr;
        this.relationType = relationType;
        this.isOrdered = opIsOrdered(expr.getOperator());
    }

    public boolean isOrdered() {
        return isOrdered;
    }

    public RelationType getRelationType() {
        return relationType;
    }

    public BinaryExpr getNode() {
        return relationNode;
    }

    /**
     * Create a Relation object of the correct type from a BinaryExpr AST node
     * 
     * @param relExpr
     * @return
     * @throws IllegalArgumentException if the operator is not a relational operator
     */
    public static Relation from(BinaryExpr relExpr) {
        Expression left = relExpr.getLeft();
        Expression right = relExpr.getRight();

        if (!opIsRelational(relExpr.getOperator())) {
            throw new IllegalArgumentException("Operator must be a relational operator: " + relExpr);
        }

        if (left.isNameExpr()) {
            if (right.isNameExpr()) {
                return new NameNameRelation(relExpr, left.asNameExpr(), right.asNameExpr());
            } else if (right.isLiteralExpr()) {
                return new NameLiteralRelation(relExpr, left.asNameExpr(), right.asLiteralExpr());
            } else {
                return new ExprNameRelation(relExpr, left.asNameExpr(), right);
            }
        }
        if (right.isNameExpr()) {
            if (left.isLiteralExpr()) {
                return new NameLiteralRelation(relExpr, right.asNameExpr(), left.asLiteralExpr());
            } else {
                return new ExprNameRelation(relExpr, right.asNameExpr(), left);
            }
        }
        if (left.isLiteralExpr()) {
            if (right.isLiteralExpr()) {
                return new LiteralLiteralRelation(relExpr, left.asLiteralExpr(), right.asLiteralExpr());
            } else {
                return new ExprLiteralRelation(relExpr, left.asLiteralExpr(), right);
            }
        }
        if (right.isLiteralExpr()) {
            return new ExprLiteralRelation(relExpr, right.asLiteralExpr(), left);
        }
        return new ExprExprRelation(relExpr, left, right);
    }

    protected static boolean opIsOrdered(BinaryExpr.Operator op) {
        return op == BinaryExpr.Operator.LESS || op == BinaryExpr.Operator.LESS_EQUALS
                || op == BinaryExpr.Operator.GREATER || op == BinaryExpr.Operator.GREATER_EQUALS;
    }

    protected static boolean opIsRelational(BinaryExpr.Operator op) {
        switch (op) {
            case LESS:
            case LESS_EQUALS:
            case GREATER:
            case GREATER_EQUALS:
            case EQUALS:
            case NOT_EQUALS:
                return true;
            default:
                return false;
        }
    }

    public boolean isNameNameRelation() {
        return false;
    }

    public boolean isExprNameRelation() {
        return false;
    }

    public boolean isNameLiteralRelation() {
        return false;
    }

    public boolean isExprLiteralRelation() {
        return false;
    }

    public boolean isLiteralLiteralRelation() {
        return false;
    }

    public boolean isExprExprRelation() {
        return false;
    }

    public NameNameRelation asNameNameRelation() {
        throw new UnsupportedOperationException("This relation is not a NameNameRelation: " + relationNode);
    }

    public ExprNameRelation asExprNameRelation() {
        throw new UnsupportedOperationException("This relation is not an ExprNameRelation: " + relationNode);
    }

    public NameLiteralRelation asNameLiteralRelation() {
        throw new UnsupportedOperationException("This relation is not a NameLiteralRelation: " + relationNode);
    }

    public ExprLiteralRelation asExprLiteralRelation() {
        throw new UnsupportedOperationException("This relation is not a ExprLiteralRelation: " + relationNode);
    }

    public LiteralLiteralRelation asLiteralLiteralRelation() {
        throw new UnsupportedOperationException("This relation is not a LiteralLiteralRelation: " + relationNode);
    }

    public ExprExprRelation asExprExprRelation() {
        throw new UnsupportedOperationException("This relation is not a ExprExprRelation: " + relationNode);
    }
}
