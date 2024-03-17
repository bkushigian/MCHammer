package org.mutation_testing;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.CharLiteralExpr;
import com.github.javaparser.ast.expr.DoubleLiteralExpr;
import com.github.javaparser.ast.expr.EnclosedExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.LongLiteralExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.TextBlockLiteralExpr;
import com.github.javaparser.ast.expr.UnaryExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;

import java.util.HashMap;
import java.util.Map;

import static org.mutation_testing.MCs.predicates;
import static org.mutation_testing.MCs.FALSE;
import static org.mutation_testing.MCs.TRUE;
import static org.mutation_testing.MCs.join;
import static org.mutation_testing.ExprUtils.lt;
import static org.mutation_testing.ExprUtils.gt;
import static org.mutation_testing.ExprUtils.isNullCheck;
import static org.mutation_testing.ExprUtils.eq;
import static org.mutation_testing.ExprUtils.neq;


public class MCsCollector extends GenericVisitorAdapter<MCs, MCs> {

    Map<MCs, Node> endBlock = new HashMap<>();

    public Map<MCs, Node> getEndBlock() {
        return endBlock;
    }

    @Override
    public MCs visit(MethodDeclaration n, MCs arg) {
        return super.visit(n, arg);
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
        // Check if this is the end of the method and if the last statement not
        // is a return. If so we want toadd the condition to endBlock
        if (n.getParentNode().isPresent() &&
                n.getParentNode().get() instanceof MethodDeclaration &&
                n.getStatements().getLast().isPresent() &&
                !(n.getStatements().getLast().get() instanceof ReturnStmt)) {
            endBlock.put(cond, n);
        }
        return cond;
    }

    @Override
    public MCs visit(IfStmt n, MCs arg) {
        // First, skip a null check
        // TODO: Handle Null Correctly
        if (isNullCheck(n.getCondition())){
            System.out.println("Skipping null check");
            return arg;
        }
        // First refine states based on condition
        MCs conditionResult = n.getCondition().accept(this, arg);
        MCs condTrue = predicates(n.getCondition().clone());
        MCs condFalse = predicates(neg(n.getCondition()));
        MCs thenResult = n.getThenStmt().accept(this, conditionResult.refine(condTrue));
        if (n.getElseStmt().isPresent()) {
            MCs elseResult = n.getElseStmt().get().accept(this,
                    conditionResult.refine(condFalse));
            return join(thenResult, elseResult);
        } else {
            return join(thenResult, conditionResult.refine(condFalse));
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
            case AND: {
                if (isNullCheck(n.getLeft())) {
                    return n.getRight().accept(this, arg);
                }
                MCs left = n.getLeft().accept(this, arg); // Condition after left
                MCs rightPC = left.refine(predicates(n.getLeft()));
                MCs right = n.getRight().accept(this, rightPC);
                return join(left.refine(predicates(neg(n.getLeft()))), right);
            }
            case OR: {
                if (isNullCheck(n.getLeft())) {
                    return n.getRight().accept(this, arg);
                }
                MCs left = n.getLeft().accept(this, arg); // Condition after left
                MCs rightPC = left.refine(predicates(neg(n.getLeft())));
                MCs right = n.getRight().accept(this, rightPC);
                return join(left.refine(predicates(n.getLeft())), right);
            }
            case EQUALS:
            case NOT_EQUALS: {
                if (n.getLeft().isNullLiteralExpr() || n.getRight().isNullLiteralExpr()) {
                    // TODO: how should we handle null checks?
                    return arg;
                }
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
                MCs rightPC = arg.refine(left);
                MCs right = n.getRight().accept(this, rightPC);
                MCs both = orderPredicates(n.getLeft(), n.getRight());
                return left.refine(right).refine(both);
            }

            default:
                return n.getLeft().accept(this, arg).refine(n.getRight().accept(this, arg));
        }
    }

    @Override
    public MCs visit(ExpressionStmt n, MCs arg) {
        return super.visit(n, arg);
    }

    @Override
    public MCs visit(MethodCallExpr n, MCs arg) {
        ResolvedMethodDeclaration r = n.resolve();
        ResolvedType rt = r.getReturnType();
        MCs mcs = super.visit(n, arg);
        if (rt.isPrimitive() && rt.asPrimitive().isBoolean()) {
            return mcs.refine(trueFalsePredicates(n));
        }
        if (rt.isReferenceType() && rt.asReferenceType().getQualifiedName().equals("java.lang.Boolean")) {
            return mcs.refine(trueFalsePredicates(n));
        }
        return mcs;
    }

    public MCs visit(UnaryExpr n, MCs arg) {

        if (n.getOperator() == UnaryExpr.Operator.LOGICAL_COMPLEMENT) {
            MCs result = n.getExpression().accept(this, arg);
            return result.refine(trueFalsePredicates(n.getExpression()));
        }
        return arg;
    }

    @Override
    public MCs visit(NameExpr n, MCs arg) {
        if (isBoolean(n.calculateResolvedType())) {
            return arg.refine(trueFalsePredicates(n));
        }
        return arg;
    }

    @Override
    public MCs visit(SimpleName n, MCs arg) {
        return arg;
    }

    @Override
    public MCs visit(LongLiteralExpr n, MCs arg) {
        return arg;
    }

    @Override
    public MCs visit(NullLiteralExpr n, MCs arg) {
        return arg;
    }

    @Override
    public MCs visit(CharLiteralExpr n, MCs arg) {
        return arg;
    }

    @Override
    public MCs visit(StringLiteralExpr n, MCs arg) {
        return arg;
    }

    @Override
    public MCs visit(DoubleLiteralExpr n, MCs arg) {
        return arg;
    }

    @Override
    public MCs visit(IntegerLiteralExpr n, MCs arg) {
        return arg;
    }

    @Override
    public MCs visit(BooleanLiteralExpr n, MCs arg) {
        return arg;
    }

    @Override
    public MCs visit(TextBlockLiteralExpr n, MCs arg) {
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

    public static MCs.Predicates trueFalsePredicates(Expression expr) {
        return predicates(expr.clone(), neg(expr.clone()));
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

    boolean isBoolean(ResolvedType t) {
        return t.isPrimitive() && t.asPrimitive().isBoolean()
                || t.isReferenceType() && t.asReferenceType().getQualifiedName().equals("java.lang.Boolean");
    }
}
