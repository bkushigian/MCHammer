package org.mutation_testing;

import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.CharLiteralExpr;
import com.github.javaparser.ast.expr.DoubleLiteralExpr;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.LongLiteralExpr;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.TextBlockLiteralExpr;
import com.github.javaparser.ast.expr.UnaryExpr;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;

public class ExpressionPropertyVisitor extends GenericVisitorAdapter<ExpressionPropertyVisitor.Properties, Void> {
    /**
     * We are interested in the following properties of an expression:
     * 
     * <ul>
     * <li>
     * <b>isSimpleRelational</b>: true if the expression is a simple
     * relational expression; that is, it is a relational operator applied
     * to two simple forms (a name or a literal).
     * </li>
     * <li>
     * <b>isSimpleArithmetic</b>: true if the expression is a simple
     * arithmetic expression; that is, if it is an arithmetic operator
     * whose operands are simple forms (a name or a literal) or another
     * arithmetic expression.
     * </li>
     * <li>
     * <b>isSimplePredicate</b>: true if the expression is a name, boolean
     * literal, or a simple relational expression.
     * </li>
     * <li>
     * <b>isSimpleLogical</b>: true if the expression is a logical operator
     * (&& or ||) whose operands are either other simple logical
     * expressions or simple predicates.
     * </li>
     * </ul>
     */
    static class Properties {
        boolean isSimpleRelational = false;
        boolean isSimpleArithmetic = false;
        boolean isSimplePredicate = false;
        boolean isSimpleLogical = false;
        boolean isName = false;
        boolean isLiteral = false;
        boolean isPure = false;

        boolean hasUnhandledProperties = false;

        boolean isTerminal() {
            return isName || isLiteral;
        }

        static Properties literal() {
            Properties p = new Properties();
            p.isLiteral = true;
            p.isPure = true;
            return p;
        }

        static Properties name() {
            Properties p = new Properties();
            p.isName = true;
            p.isPure = true;
            return p;
        }

        static Properties unhandled() {
            Properties p = new Properties();
            p.hasUnhandledProperties = true;
            return p;
        }

        @Override
        public String toString() {
            return "Properties [isSimpleRelational=" + isSimpleRelational + ", isSimpleArithmetic=" + isSimpleArithmetic
                    + ", isSimplePredicate=" + isSimplePredicate + ", isSimpleLogical=" + isSimpleLogical + ", isName="
                    + isName + ", isLiteral=" + isLiteral + ", hasUnhandledProperties=" + hasUnhandledProperties +
                    ", isPure=" + isPure + "]";
        }
    }

    @Override
    public Properties visit(BinaryExpr n, Void arg) {
        Properties left;
        Properties right;

        left = n.getLeft().accept(this, arg);
        right = n.getRight().accept(this, arg);

        if (left == null) {
            left = Properties.unhandled();
        }

        if (right == null) {
            right = Properties.unhandled();
        }

        Properties p = new Properties();
        p.hasUnhandledProperties = left.hasUnhandledProperties || right.hasUnhandledProperties;
        p.isPure = left.isPure && right.isPure;

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
                p.isSimpleRelational = left.isTerminal() && right.isTerminal() && left.isPure && right.isPure;
                p.isSimplePredicate = p.isSimpleRelational;
                break;
            case AND:
            case OR:
                p.isSimpleLogical = (left.isSimpleLogical || left.isSimplePredicate)
                        && (right.isSimpleLogical || right.isSimplePredicate) && left.isPure && right.isPure;
                break;
            case PLUS:
            case MINUS:
            case MULTIPLY:
            case DIVIDE:
            case REMAINDER:
                p.isSimpleArithmetic = (left.isTerminal() || left.isSimpleArithmetic)
                        && (right.isTerminal() || right.isSimpleArithmetic);
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
        return p;
    }

    @Override
    public Properties visit(UnaryExpr n, Void arg) {
        Properties rand;
        rand = n.getExpression().accept(this, arg);
        Properties p = new Properties();
        p.hasUnhandledProperties = rand.hasUnhandledProperties;
        p.isPure = rand.isPure;
        switch (n.getOperator()) {
            case LOGICAL_COMPLEMENT:

            case MINUS:
            case PLUS:
                p.isSimpleArithmetic = rand.isPure && (rand.isTerminal() || rand.isSimpleArithmetic);
                p.isPure = rand.isPure;
                break;
            case POSTFIX_DECREMENT:
            case POSTFIX_INCREMENT:
            case PREFIX_DECREMENT:
            case PREFIX_INCREMENT:
                p.isPure = false;
                p.hasUnhandledProperties = true;
                break;
            case BITWISE_COMPLEMENT:
            default:
                p.hasUnhandledProperties = true;
                break;
        }
        return p;
    }

    /// All literals

    @Override
    public Properties visit(LongLiteralExpr n, Void arg) {
        return Properties.literal();
    }

    @Override
    public Properties visit(NullLiteralExpr n, Void arg) {
        return Properties.literal();
    }

    @Override
    public Properties visit(CharLiteralExpr n, Void arg) {
        return Properties.literal();
    }

    @Override
    public Properties visit(IntegerLiteralExpr n, Void arg) {
        return Properties.literal();
    }

    @Override
    public Properties visit(StringLiteralExpr n, Void arg) {
        return Properties.literal();
    }

    @Override
    public Properties visit(DoubleLiteralExpr n, Void arg) {
        return Properties.literal();
    }

    @Override
    public Properties visit(BooleanLiteralExpr n, Void arg) {
        return Properties.literal();
    }

    @Override
    public Properties visit(TextBlockLiteralExpr n, Void arg) {
        return Properties.literal();
    }

    /// All names
    @Override
    public Properties visit(Name n, Void arg) {
        return Properties.name();
    }

    @Override
    public Properties visit(NameExpr n, Void arg) {
        return Properties.name();
    }

    @Override
    public Properties visit(SimpleName n, Void arg) {
        return Properties.name();
    }

}
