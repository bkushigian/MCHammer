package org.mutation_testing;

import java.util.StringJoiner;

import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.BinaryExpr.Operator;
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
        boolean isSimpleName = false;
        boolean isLiteral = false;
        boolean isPure = false;

        boolean hasUnhandledProperties = false;

        boolean canMutate() {
            return isSimpleRelational || isSimplePredicate || isSimpleLogical;
        }

        boolean isTerminal() {
            return isName || isSimpleName || isLiteral;
        }

        boolean isName() {
            return isName;
        }

        boolean isSimpleName() {
            return isSimpleName;
        }

        boolean isLiteral() {
            return isLiteral;
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

        static Properties simpleName() {
            Properties p = new Properties();
            p.isSimpleName = true;
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
                    + isName + ", isSimpleName=" + isSimpleName + ", isLiteral=" + isLiteral
                    + ", hasUnhandledProperties=" + hasUnhandledProperties + ", isPure=" + isPure + "]";
        }

        public String summarizeProperties() {
            StringJoiner sj = new StringJoiner(", ");
            if (isSimpleLogical) {
                sj.add("Simple Logical");
            } else if (isSimpleRelational) {
                sj.add("Simple Relational");
            } else if (isSimpleArithmetic) {
                sj.add("Simple Arithmetic");
            } else if (isSimplePredicate) {
                sj.add("Simple Predicate");
            } else if (isName) {
                sj.add("Name");
            } else if (isSimpleName) {
                sj.add("SimpleName");
            } else if (isLiteral) {
                sj.add("Literal");
            } else if (hasUnhandledProperties) {
                sj.add("Unhandled");
            }

            return sj.toString();

        }
    }

    boolean opIsRelational(BinaryExpr.Operator op) {
        switch (op) {
            case EQUALS:
            case NOT_EQUALS:
            case GREATER:
            case GREATER_EQUALS:
            case LESS:
            case LESS_EQUALS:
                return true;
            default:
                return false;
        }
    }

    boolean opIsArithmetic(BinaryExpr.Operator op) {
        switch (op) {
            case PLUS:
            case MINUS:
            case MULTIPLY:
            case DIVIDE:
            case REMAINDER:
                return true;
            default:
                return false;
        }
    }

    boolean opIsLogical(BinaryExpr.Operator op) {
        switch (op) {
            case AND:
            case OR:
                return true;
            default:
                return false;
        }
    }

    boolean opIsBitwise(BinaryExpr.Operator op) {
        switch (op) {
            case BINARY_AND:
            case BINARY_OR:
            case XOR:
            case LEFT_SHIFT:
            case SIGNED_RIGHT_SHIFT:
            case UNSIGNED_RIGHT_SHIFT:
                return true;
            default:
                return false;
        }
    }

    Properties combineProperties(Operator op, Properties left, Properties right) {
        Properties p = new Properties();
        p.hasUnhandledProperties = left.hasUnhandledProperties || right.hasUnhandledProperties;
        p.isPure = left.isPure && right.isPure;
        if (p.hasUnhandledProperties) {
            return p;
        }

        if (opIsRelational(op)) {
            p.isSimpleRelational = left.isSimpleName() && right.isLiteral() || left.isLiteral() && right.isSimpleName();
            p.isSimplePredicate = p.isSimpleRelational;
        } else if (opIsArithmetic(op)) {
            p.isSimpleArithmetic = (left.isTerminal() || left.isSimpleArithmetic)
                    && (right.isTerminal() || right.isSimpleArithmetic);
        } else if (opIsLogical(op)) {
            p.isSimpleLogical = (left.isSimpleLogical || left.isSimplePredicate)
                    && (right.isSimpleLogical || right.isSimplePredicate);
        } else {
            p.hasUnhandledProperties = true;
        }
        return p;
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
        return combineProperties(n.getOperator(), left, right);
    }

    boolean isIncDec(UnaryExpr.Operator op) {
        switch (op) {
            case POSTFIX_DECREMENT:
            case POSTFIX_INCREMENT:
            case PREFIX_DECREMENT:
            case PREFIX_INCREMENT:
                return true;
            default:
                return false;
        }
    }

    boolean isLogicalNot(UnaryExpr.Operator op) {
        return op == UnaryExpr.Operator.LOGICAL_COMPLEMENT;
    }

    boolean isArithmeticUnary(UnaryExpr.Operator op) {
        return op == UnaryExpr.Operator.MINUS || op == UnaryExpr.Operator.PLUS;
    }

    Properties combineProperties(UnaryExpr.Operator op, Properties operand) {
        Properties p = new Properties();
        p.hasUnhandledProperties = operand.hasUnhandledProperties;
        p.isPure = operand.isPure;
        if (p.hasUnhandledProperties) {
            return p;
        }

        if (isIncDec(op)) {
            p.isPure = false;
            p.hasUnhandledProperties = true;
        } else if (isLogicalNot(op)) {
            p.isSimpleLogical = operand.isSimplePredicate;
        } else if (isArithmeticUnary(op)) {
            p.isSimpleArithmetic = operand.isPure && (operand.isTerminal() || operand.isSimpleArithmetic);
        } else {
            p.hasUnhandledProperties = true;
        }
        return p;
    }

    @Override
    public Properties visit(UnaryExpr n, Void arg) {
        Properties rand = n.getExpression().accept(this, arg);
        return combineProperties(n.getOperator(), rand);
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
        return Properties.simpleName();
    }
}
