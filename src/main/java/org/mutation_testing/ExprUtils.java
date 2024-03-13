package org.mutation_testing;

import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.ConditionalExpr;
import com.github.javaparser.ast.expr.EnclosedExpr;
import com.github.javaparser.ast.expr.Expression;

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


    public static boolean isOr(Expression e) {
        return e instanceof BinaryExpr && ((BinaryExpr) e).getOperator().equals(BinaryExpr.Operator.OR);
    }

    public static boolean isAnd(Expression e) {
        return e instanceof BinaryExpr && ((BinaryExpr) e).getOperator().equals(BinaryExpr.Operator.AND);
    }

    public static boolean isConditionalExpr(Expression e) {
        return e instanceof ConditionalExpr;
    }

}