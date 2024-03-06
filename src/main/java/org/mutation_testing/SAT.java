package org.mutation_testing;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.resolution.types.ResolvedType;
import com.microsoft.z3.*;

public class SAT {

    boolean checkPredicate(Expression e, Map<NameExpr, ResolvedType> types) {
        Context ctx = new Context();
        Solver s = ctx.mkSolver();
        // TODO: Check the predicate

        ctx.close();
        return false;
    }

    /**
     * Special case of refinement: if there is a conjunction of two relational
     * operators on the same variables we can simplify the expression:
     * 
     * <ul>
     * <li>
     * a == b && a < b -> false
     * </li>
     * <li>
     * a >= b && a > b -> a > b
     * </li>
     * </ul>
     * 
     * 
     * @param a
     * @param b
     * @return
     */
    public static Expression simplifyRelOpConjunction(BinaryExpr a, BinaryExpr b) {
        Expression aLeft = a.getLeft(), aRight = a.getRight(), bLeft = b.getLeft(), bRight = b.getRight();
        BinaryExpr b2 = b;

        if (aLeft.equals(bRight) && aRight.equals(bLeft)) {
            b2 = reverseBinaryExpr(b2);
            bLeft = b2.getLeft();
            bRight = b2.getRight();
        }

        if (!aLeft.equals(bLeft) || !aRight.equals(bRight)) {
            return and(a, b);
        }

        BinaryExpr.Operator aOp = a.getOperator();
        BinaryExpr.Operator bOp = b2.getOperator();

        if (aOp.equals(bOp)) {
            return a;
        }

        if (isFinerThan(aOp, bOp)) {
            return a;
        }
        if (isFinerThan(bOp, aOp)) {
            return b;
        }

        return null;
    }

    static boolean isFinerThan(BinaryExpr.Operator aOp, BinaryExpr.Operator bOp) {
        if (aOp.equals(bOp)) {
            return true;
        }
        switch (aOp) {
            case EQUALS:
                switch (bOp) {
                    case GREATER_EQUALS:
                    case LESS_EQUALS:
                    case EQUALS:
                        return true;
                    default:
                        return false;
                }
            case NOT_EQUALS:
                return false;
            case GREATER:
                switch (bOp) {
                    case GREATER_EQUALS:
                        return true;
                    default:
                        return false;
                }
            case LESS:
                switch (bOp) {
                    case LESS_EQUALS:
                        return true;
                    default:
                        return false;
                }
            case LESS_EQUALS:
            case GREATER_EQUALS:
                return false;
            default:
                return false;
        }
    }

    static Expression and(Expression lhs, Expression rhs) {
        return new BinaryExpr(lhs.clone(), rhs.clone(), BinaryExpr.Operator.AND);
    }

    static Expression or(Expression lhs, Expression rhs) {
        return new BinaryExpr(lhs.clone(), rhs.clone(), BinaryExpr.Operator.OR);
    }

    static BinaryExpr reverseBinaryExpr(BinaryExpr binExpr) {
        BinaryExpr.Operator op = binExpr.getOperator();
        switch (binExpr.getOperator()) {
            case EQUALS:
                op = BinaryExpr.Operator.NOT_EQUALS;
                break;
            case NOT_EQUALS:
                op = BinaryExpr.Operator.EQUALS;
                break;
            case GREATER:
                op = BinaryExpr.Operator.LESS;
                break;
            case GREATER_EQUALS:
                op = BinaryExpr.Operator.LESS_EQUALS;
                break;
            case LESS:
                op = BinaryExpr.Operator.GREATER;
                break;
            case LESS_EQUALS:
                op = BinaryExpr.Operator.GREATER_EQUALS;
                break;
            default:
                break;
        }
        return new BinaryExpr(binExpr.getRight().clone(), binExpr.getLeft().clone(), op);
    }
}
