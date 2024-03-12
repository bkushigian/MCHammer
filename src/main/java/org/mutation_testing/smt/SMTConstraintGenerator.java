package org.mutation_testing.smt;

import static org.mutation_testing.ExpressionData.DATA_KEY;

import org.mutation_testing.ExpressionData;
import org.mutation_testing.NotImplementedException;

import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.CharLiteralExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.LongLiteralExpr;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;
import com.github.javaparser.resolution.types.ResolvedPrimitiveType;
import com.github.javaparser.resolution.types.ResolvedType;
import com.microsoft.z3.*;

public class SMTConstraintGenerator extends GenericVisitorAdapter<Expr<? extends Sort>, Context> {

    public static Expr<? extends Sort> generateConstraints(Expression e, Context ctx) {
        return e.accept(new SMTConstraintGenerator(), ctx);
    }


    // @Override
    // public Expr<? extends Sort> visit(Name n, Context ctx) {
    //     ExpressionData data = n.getData(DATA_KEY);
    //     return ctx.mkConst(n.asString(), typeToSort(data.type, ctx));
    // }

    @Override
    public Expr<? extends Sort> visit(NameExpr n, Context ctx) {
        ExpressionData data = n.getData(DATA_KEY);
        return ctx.mkConst(n.getNameAsString(), typeToSort(data.type, ctx));
    }

    @Override
    public Expr<? extends Sort> visit(BinaryExpr n, Context ctx) {
        Expr<? extends Sort> lhs = n.getLeft().accept(this, ctx);
        Expr<? extends Sort> rhs = n.getRight().accept(this, ctx);
        switch (n.getOperator()) {
            /// Logical
            case AND:
                return ctx.mkAnd((BoolExpr)lhs, (BoolExpr)rhs);
            case OR:
                return ctx.mkOr((BoolExpr)lhs, (BoolExpr)rhs);

            /// Relational Ops
            // TODO: Currently assuming all comparisons are signed, which is not
            //       true for chars
            case EQUALS:
                return ctx.mkEq(lhs, rhs);
            case NOT_EQUALS:
                return ctx.mkNot(ctx.mkEq(lhs, rhs));
            case GREATER:
                return ctx.mkBVSGT((Expr<BitVecSort>)lhs, (Expr<BitVecSort>)rhs);
            case GREATER_EQUALS:
                return ctx.mkBVSGE((Expr<BitVecSort>)lhs, (Expr<BitVecSort>)rhs);
            case LESS:
                return ctx.mkBVSLT((Expr<BitVecSort>)lhs, (Expr<BitVecSort>)rhs);
            case LESS_EQUALS:
                return ctx.mkBVSLE((Expr<BitVecSort>)lhs, (Expr<BitVecSort>)rhs);

            /// Binary Ops
            case BINARY_AND:
                break;
            case BINARY_OR:
                break;
            case XOR:
                break;

            /// Arithmetic Ops
            case DIVIDE:
                break;
            case MINUS:
                break;
            case MULTIPLY:
                break;
            case PLUS:
                break;
            case REMAINDER:
                break;

            /// Shift
            case LEFT_SHIFT:
                break;
            case SIGNED_RIGHT_SHIFT:
                break;
            case UNSIGNED_RIGHT_SHIFT:
                break;
            default:
                break;

        }
        throw new NotImplementedException();
    }

    @Override
    public Expr<? extends Sort> visit(BooleanLiteralExpr n, Context ctx) {
        return ctx.mkBool(n.getValue());
    }

    @Override
    public Expr<? extends Sort> visit(IntegerLiteralExpr n, Context ctx) {
        return ctx.mkBV(n.getValue(), 32);
    }

    @Override
    public Expr<? extends Sort> visit(LongLiteralExpr n, Context ctx) {
        return ctx.mkBV(n.getValue(), 64);
    }

    public Expr<? extends Sort> visit(CharLiteralExpr n, Context ctx) {
        return ctx.mkBV(n.getValue().charAt(0), 16);
    }

    /**
     * Get a Z3 sort for a given Java type
     * @param t
     * @param ctx
     * @return
     */
    private Sort typeToSort(ResolvedType t, Context ctx) { 
        if (t.isReferenceType()) {
            throw new NotImplementedException("Reference Types");
            //todo
        } else if (t.isPrimitive()){
            ResolvedPrimitiveType pt = t.asPrimitive();
            switch (pt) {
                case BOOLEAN:
                    return ctx.mkBoolSort();
                case BYTE:
                    return ctx.mkBitVecSort(8);
                case CHAR:
                    return ctx.mkBitVecSort(16);
                case INT:
                    return ctx.mkBitVecSort(32);
                case LONG:
                    return ctx.mkBitVecSort(64);
                case SHORT:
                    return ctx.mkBitVecSort(16);
                case DOUBLE:
                case FLOAT:
                    throw new NotImplementedException("Floating Point");

            }
        } else if (t.isArray()) {
            throw new NotImplementedException("Array Types");
        }
        throw new IllegalStateException("Neither reference or primitive");
    }
    
}
