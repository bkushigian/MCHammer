package org.mutation_testing;

import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.Expression;
import static com.github.javaparser.ast.expr.BinaryExpr.Operator.*;

import java.util.ArrayList;
import java.util.List;

public class MCsOptimizer {
    
    /**
     * Simplifies a list of mutation conditions by removing any that are
     * logically entailed by another, taking care to handle the case where the
     * same condition is listed twice.
     * @param mcs
     * @return
     */
    public static List<Expression> simplifyConjunctionOperands(List<Expression> mcs) {
        final List<Expression> simplified = new ArrayList<>(mcs);

        for (int i = 0; i < simplified.size(); i++) {
            for (int j = i + 1; j < simplified.size(); j++) {
                Expression mc1 = simplified.get(i);
                Expression mc2 = simplified.get(j);
                if (logicallyEntails(mc1, mc2)) {
                    simplified.remove(mc2);
                    j--;
                } else if (logicallyEntails(mc2, mc1)) {
                    simplified.remove(mc1);
                    i--;
                    break;
                }
            }
        }
        return simplified;
    }

    public static Expression simplifyConjunction(Expression e) {
        List<Expression> operands = flatConjunctionOperands(e);
        List<Expression> simplified = simplifyConjunctionOperands(operands);
        if (simplified.size() == 0) {
            return new BooleanLiteralExpr(true);
        }
        if (simplified.size() == 1) {
            return simplified.get(0);
        }
        BinaryExpr result = new BinaryExpr(simplified.get(0), simplified.get(1), AND);
        for (int i = 2; i < simplified.size(); i++) {
            result = new BinaryExpr(result, simplified.get(i), AND);
        }
        return result;

    }

    static List<Expression> flatConjunctionOperands(Expression e) {
        List<Expression> result = new ArrayList<>();
        if (e.isBinaryExpr() && e.asBinaryExpr().getOperator() == BinaryExpr.Operator.AND) {
            BinaryExpr b = e.asBinaryExpr();
            result.addAll(flatConjunctionOperands(b.getLeft()));
            result.addAll(flatConjunctionOperands(b.getRight()));
        } else {
            result.add(e);
        }
        return result;
    }


    static boolean logicallyEntails(Expression precedent, Expression antecedent) {
        if (precedent.equals(antecedent)) {
            return true;
        }

        // False entails everything, true entails only itself
        if (precedent.isBooleanLiteralExpr()) {
            return !precedent.asBooleanLiteralExpr().getValue();
        }

        // Everything is entailed by true, nothing is entailed by false
        if (antecedent.isBooleanLiteralExpr()) {
            return antecedent.asBooleanLiteralExpr().getValue();
        }

        if (precedent.isBinaryExpr() && antecedent.isBinaryExpr()) {
            return relOpLogicallyEntails(precedent.asBinaryExpr(), antecedent.asBinaryExpr());
        }

        return false;

    }

    static boolean relOpLogicallyEntails(BinaryExpr precedent, BinaryExpr antecedent) {
        if (!isRelOp(precedent.getOperator()) || !isRelOp(antecedent.getOperator())) {
            return false;
        }
        if (precedent.equals(antecedent)) {
            return true;
        }

        if (sameOperands(precedent, antecedent)) {
            return relOpLogicallyEntails(precedent.getOperator(), antecedent.getOperator());
        }

        if (swappedOperands(precedent, antecedent)) {
            return relOpLogicallyEntails(precedent.getOperator(), reverseOperator(antecedent.getOperator()));
        }
        return false;
    }

    /**
     * Assuming that both operands are identical, does the first operator
     * logically entail the second?
     * 
     * Returns `false` if the operators are not relational operators
     * @param op1
     * @param op2
     * @return
     */
    public static boolean relOpLogicallyEntails(BinaryExpr.Operator op1, BinaryExpr.Operator op2) {
        if (op1 == op2) {
            return true;
        }
        switch (op1) {
            case EQUALS:
                return op2 == LESS_EQUALS || op2 == GREATER_EQUALS;
            case NOT_EQUALS:
                return false;
            case GREATER:
                return op2 == GREATER_EQUALS || op2 == NOT_EQUALS;
            case GREATER_EQUALS:
                return false;
            case LESS:
                return op2 == LESS_EQUALS || op2 == NOT_EQUALS;
            case LESS_EQUALS:
                return false;
            default:
                return false;
        }
    }

    public static boolean isRelOp(BinaryExpr.Operator op) {
        return op == EQUALS || op == NOT_EQUALS || op == GREATER || op == GREATER_EQUALS || op == LESS || op == LESS_EQUALS;
    }

    public static boolean isArithOp(BinaryExpr.Operator op) {
        return op == PLUS || op == MINUS || op == MULTIPLY || op == DIVIDE || op == REMAINDER;
    }

    public static boolean isConditionalOp(BinaryExpr.Operator op) {
        return op == AND || op == OR;
    }

    public static boolean isBitwiseOp(BinaryExpr.Operator op) {
        return op == BINARY_AND || op == BINARY_OR || op == XOR || op == LEFT_SHIFT || op == SIGNED_RIGHT_SHIFT || op == UNSIGNED_RIGHT_SHIFT;
    }

    public static boolean sameOperands(BinaryExpr op1, BinaryExpr op2) {
        return op1.getLeft().equals(op2.getLeft()) && op1.getRight().equals(op2.getRight());
    }

    public static boolean swappedOperands(BinaryExpr op1, BinaryExpr op2) {
        return op1.getLeft().equals(op2.getRight()) && op1.getRight().equals(op2.getLeft());
    }

    /**
     * Returns the reverse of the given operator (so < becomes >, <= becomes >=, etc.)
     * @param op
     * @return
     */
    public static BinaryExpr.Operator reverseOperator(BinaryExpr.Operator op) {
        switch (op) {
            case EQUALS:
                return EQUALS;
            case NOT_EQUALS:
                return NOT_EQUALS;
            case GREATER:
                return LESS;
            case GREATER_EQUALS:
                return LESS_EQUALS;
            case LESS:
                return GREATER;
            case LESS_EQUALS:
                return GREATER_EQUALS;
            default:
                return null;
        }
    }

    public static BinaryExpr.Operator negateOperator(BinaryExpr.Operator op) {
        switch (op) {
            case EQUALS:
                return NOT_EQUALS;
            case NOT_EQUALS:
                return EQUALS;
            case GREATER:
                return LESS_EQUALS;
            case GREATER_EQUALS:
                return LESS;
            case LESS:
                return GREATER_EQUALS;
            case LESS_EQUALS:
                return GREATER;
            default:
                return null;
        }
    }

}
