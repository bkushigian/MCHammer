package org.mutation_testing;

import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.ConditionalExpr;
import com.github.javaparser.ast.expr.EnclosedExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.UnaryExpr;

public class ExprUtils {
    public static Expression and(Expression lhs, Expression rhs) {
        Expression l = lhs.clone();
        Expression r = rhs.clone();
        if (isOr(l) || isConditionalExpr(l)) {
            l = enclose(l);
        }
        if (isOr(r) || isConditionalExpr(r)) {
            r = enclose(r);
        }
        return new BinaryExpr(l, r, BinaryExpr.Operator.AND);
    }

    public static Expression or(Expression lhs, Expression rhs) {
        Expression l = lhs.clone();
        Expression r = rhs.clone();
        if (isConditionalExpr(l)) {
            l = enclose(l);
        }
        if (isConditionalExpr(r)) {
            r = enclose(r);
        }
        return new BinaryExpr(lhs.clone(), rhs.clone(), BinaryExpr.Operator.OR);
    }

    public static Expression enclose(Expression e) {
        if (e instanceof EnclosedExpr) {
            return e;
        }
        return new EnclosedExpr(e);
    }

    public static Expression not(Expression e) {
        return new UnaryExpr(e, UnaryExpr.Operator.LOGICAL_COMPLEMENT);
    }

    public static Expression eq(Expression left, Expression right) {
        return new BinaryExpr(left, right, BinaryExpr.Operator.EQUALS);
    }

    public static Expression neq(Expression left, Expression right) {
        return new BinaryExpr(left, right, BinaryExpr.Operator.NOT_EQUALS);
    }

    public static Expression gt(Expression left, Expression right) {
        return new BinaryExpr(left, right, BinaryExpr.Operator.GREATER);
    }

    public static Expression ge(Expression left, Expression right) {
        return new BinaryExpr(left, right, BinaryExpr.Operator.GREATER_EQUALS);
    }

    public static Expression lt(Expression left, Expression right) {
        return new BinaryExpr(left, right, BinaryExpr.Operator.LESS);
    }

    public static Expression le(Expression left, Expression right) {
        return new BinaryExpr(left, right, BinaryExpr.Operator.LESS_EQUALS);
    }

    public static boolean isOr(Expression e) {
        return e instanceof BinaryExpr && ((BinaryExpr) e).getOperator().equals(BinaryExpr.Operator.OR);
    }

    public static boolean isAnd(Expression e) {
        return e instanceof BinaryExpr && ((BinaryExpr) e).getOperator().equals(BinaryExpr.Operator.AND);
    }

    public static boolean isConditionalExpr(Expression e) {
        return e instanceof ConditionalExpr;
    }

    public static boolean isNullCheck(Expression e) {
        if (! (e instanceof BinaryExpr)) {
            return false;
        }
        BinaryExpr be = (BinaryExpr) e;
        switch (be.getOperator()) {
            case EQUALS:
            case NOT_EQUALS:
                return be.getRight().isNullLiteralExpr() || be.getLeft().isNullLiteralExpr();
            default:
                return false;
        }
    }

}