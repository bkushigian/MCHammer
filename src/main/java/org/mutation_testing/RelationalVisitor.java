package org.mutation_testing;

import java.util.List;

import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

/**
 * A visitor to collect all relations within a tree.
 */
public class RelationalVisitor extends VoidVisitorAdapter<List<Expression>> {
    String currentMethod = null;

    protected boolean isTerminal(Expression e) {
        return e.isNameExpr() || e.isLiteralExpr();
    }

    @Override
    public void visit(BinaryExpr n, List<Expression> arg) {
        switch (n.getOperator()) {
            case EQUALS:
            case NOT_EQUALS:
            case GREATER:
            case GREATER_EQUALS:
            case LESS:
            case LESS_EQUALS:
                if (arg != null) {
                    arg.add(n);
                }
                return;
            default:
        }
        super.visit(n, arg);
    }
}
