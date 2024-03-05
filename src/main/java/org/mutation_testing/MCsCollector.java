package org.mutation_testing;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.EnclosedExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.UnaryExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;

import static org.mutation_testing.MCs.predicates;

import java.util.HashMap;
import java.util.Map;

import static org.mutation_testing.MCs.FALSE;
import static org.mutation_testing.MCs.TRUE;
import static org.mutation_testing.MCs.join;

public class MCsCollector extends GenericVisitorAdapter<MCs, MCs> {

    Map<MCs, Node> endBlock = new HashMap<>();

    @Override
    public MCs visit(MethodDeclaration n, MCs arg) {
        throw new NotImplementedException("Invoke 'collectMutationConditions' directly");
    }

    public MCs collectMutationConditions(MethodDeclaration n) {
        return collectMutationConditions(n, TRUE);
    }

    public MCs collectMutationConditions(MethodDeclaration n, MCs initialConditions) {
        return n.accept(this, initialConditions);
    }

    @Override
    public MCs visit(BlockStmt n, MCs cond) {
        for (Statement stmt : n.getStatements()) {
            cond = cond.refine(stmt.accept(this, cond));
        }
        return cond;
    }

    @Override
    public MCs visit(IfStmt n, MCs arg) {
        MCs conditionResult = n.getCondition().accept(this, arg);
        MCs condTrue = predicates(n.getCondition().clone());
        MCs condFalse = predicates(neg(n.getCondition()));
        MCs thenResult = n.getThenStmt().accept(this, conditionResult.refine(condTrue));
        if (n.getElseStmt().isPresent()) {
            MCs elseResult = n.getElseStmt().get().accept(this,
                    conditionResult.refine(condFalse));
            return join(thenResult, elseResult);
        } else {
            return join(thenResult, condFalse);
        }
    }

    @Override
    public MCs visit(ReturnStmt n, MCs arg) {
        MCs result = n.getExpression().map(e -> e.accept(this, arg)).orElse(arg);
        endBlock.put(result, n);
        return FALSE;
    }

    public MCs visit(BinaryExpr n, MCs arg) {
        switch (n.getOperator()) {
            case AND:
            case OR: {
                MCs left = n.getLeft().accept(this, arg);
                MCs right = n.getRight().accept(this, arg);
                return left.refine(right);
            }
            case EQUALS:
            case NOT_EQUALS: {
                // TODO: Handle like if/else with a join
                MCs left = n.getLeft().accept(this, arg);
                MCs right = n.getRight().accept(this, arg);
                MCs both = eqPredicates(n.getLeft(), n.getRight());
                return left.refine(right).refine(both);
            }
            case GREATER:
            case GREATER_EQUALS:
            case LESS:
            case LESS_EQUALS: {
                MCs left = n.getLeft().accept(this, arg);
                MCs right = n.getRight().accept(this, left);
                MCs both = orderPredicates(n.getLeft(), n.getRight());
                return left.refine(right).refine(both);
            }

            default:
                return n.getLeft().accept(this, arg).refine(n.getRight().accept(this, arg));
        }
    }

    public MCs visit(UnaryExpr n, MCs arg) {

        if (n.getOperator() == UnaryExpr.Operator.LOGICAL_COMPLEMENT) {
            MCs result = n.getExpression().accept(this, arg);
            return result.refine(negPredicates(n.getExpression()));
        }
        return arg;
    }

    /// AST HELPER METHODS

    public static MCs.Predicates eqPredicates(Expression left, Expression right) {
        return predicates(eq(left.clone(), right.clone()), neq(left.clone(), right.clone()));
    }

    public static MCs.Predicates orderPredicates(Expression left, Expression right) {
        return predicates(
                lt(left.clone(), right.clone()),
                eq(left.clone(), right.clone()),
                gt(left.clone(), right.clone()));
    }

    public static MCs.Predicates negPredicates(Expression expr) {
        return predicates(expr.clone(), neg(expr.clone()));
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

    public static Expression parens(Expression expr) {
        if (expr.isEnclosedExpr()) {
            return expr;
        }
        return new EnclosedExpr(expr);
    }

    public static Expression neg(Expression expr) {
        if (expr.isUnaryExpr()) {
            UnaryExpr unary = expr.asUnaryExpr();
            if (unary.getOperator() == UnaryExpr.Operator.LOGICAL_COMPLEMENT) {
                return unary.getExpression().clone();
            }
        } else if (expr.isBinaryExpr()) {
            BinaryExpr binary = expr.asBinaryExpr();
            switch (binary.getOperator()) {
                case EQUALS:
                    return new BinaryExpr(binary.getLeft().clone(), binary.getRight().clone(),
                            BinaryExpr.Operator.NOT_EQUALS);
                case NOT_EQUALS:
                    return new BinaryExpr(binary.getLeft().clone(), binary.getRight().clone(),
                            BinaryExpr.Operator.EQUALS);
                case GREATER:
                    return new BinaryExpr(binary.getLeft().clone(), binary.getRight().clone(),
                            BinaryExpr.Operator.LESS_EQUALS);
                case GREATER_EQUALS:
                    return new BinaryExpr(binary.getLeft().clone(), binary.getRight().clone(),
                            BinaryExpr.Operator.LESS);
                case LESS:
                    return new BinaryExpr(binary.getLeft().clone(), binary.getRight().clone(),
                            BinaryExpr.Operator.GREATER_EQUALS);
                case LESS_EQUALS:
                    return new BinaryExpr(binary.getLeft().clone(), binary.getRight().clone(),
                            BinaryExpr.Operator.GREATER);
                case AND:
                    return new BinaryExpr(neg(binary.getLeft()), neg(binary.getRight()),
                            BinaryExpr.Operator.OR);
                case OR:
                    return new BinaryExpr(neg(binary.getLeft()), neg(binary.getRight()),
                            BinaryExpr.Operator.AND);
                default:
                    break;
            }
            return new UnaryExpr(expr.clone(), UnaryExpr.Operator.LOGICAL_COMPLEMENT);
        }
        return new UnaryExpr(expr.clone(), UnaryExpr.Operator.LOGICAL_COMPLEMENT);
    }
}
