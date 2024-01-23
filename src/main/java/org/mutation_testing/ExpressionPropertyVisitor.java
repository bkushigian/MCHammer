package org.mutation_testing;

import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.UnaryExpr;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;

public class ExpressionPropertyVisitor extends GenericVisitorAdapter<ExpressionPropertyVisitor.Properties, Void> {
    /**
     * We are interested in the following properties of an expression:
     * 
     * <ul> 
     *  <li> 
     *      <b>isSimpleRelational</b>: true if the expression is a simple
     *      relational expression; that is, it is a relational operator applied
     *      to two simple forms (a name or a literal).
     *  </li>
     *  <li>
     *      <b>isSimpleArithmetic</b>: true if the expression is a simple
     *      arithmetic expression; that is, if it is an arithmetic operator
     *      whose operands are simple forms (a name or a literal) or another
     *      arithmetic expression.
     *  </li>
     *  <li>
     *      <b>isSimplePredicate</b>: true if the expression is a name, boolean
     *      literal, or a simple relational expression.
     *   </li>
     *   <li>
     *      <b>isSimpleLogical</b>: true if the expression is a logical operator
     *      (&& or ||) whose operands are either other simple logical
     *      expressions or simple predicates.
     *  </li>
     * </ul>
     */
    static class Properties {
        boolean isSimpleRelational = false;
        boolean isSimpleArithmetic = false;
        boolean isSimplePredicate = false;
        boolean isSimpleLogical = false;
        boolean isName = false;
        boolean isLiteral = false;

        boolean hasUnhandledProperties = false;

        boolean isTerminal() {
            return isName || isLiteral;
        }
    }

    @Override
    public Properties visit(BinaryExpr n, Void arg) {
        Properties left, right; 
        left = n.getLeft().accept(this, arg);
        right = n.getRight().accept(this, arg);
        Properties p = new Properties();
        p.hasUnhandledProperties = left.hasUnhandledProperties || right.hasUnhandledProperties;

        if (p.hasUnhandledProperties) {
            return p;
        }

        switch (n.getOperator()) {
            case EQUALS:
            case NOT_EQUALS:
            case GREATER:
            case GREATER_EQUALS:
            case LESS:
            case LESS_EQUALS:
                p.isSimpleRelational = left.isTerminal() && right.isTerminal();
                p.isSimplePredicate = p.isSimpleRelational;
                break;
            case AND:
            case OR:
                p.isSimpleLogical = (left.isSimpleLogical || left.isSimplePredicate) && (right.isSimpleLogical || right.isSimplePredicate);
                break;
            case PLUS:
            case MINUS:
            case MULTIPLY:
            case DIVIDE:
            case REMAINDER:
                p.isSimpleArithmetic = (left.isTerminal() || left.isSimpleArithmetic) && (right.isTerminal() || right.isSimpleArithmetic);
                break;
            case BINARY_AND:
            case BINARY_OR:
            case XOR:
            case LEFT_SHIFT:
            case SIGNED_RIGHT_SHIFT:
            case UNSIGNED_RIGHT_SHIFT:
            default:
                break;
        }
        return super.visit(n, arg);
    }

    @Override
    public Properties visit(UnaryExpr n, Void arg) {
        Properties rand;
        rand = n.getExpression().accept(this, arg);
        Properties p = new Properties();
        p.hasUnhandledProperties = rand.hasUnhandledProperties;
        return super.visit(n, arg);
    }



}
