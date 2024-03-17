package org.mutation_testing.smt;

import static org.mutation_testing.ExpressionData.DATA_KEY;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mutation_testing.ExpressionData;
import org.mutation_testing.NotImplementedException;

import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.CharLiteralExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.LongLiteralExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.UnaryExpr;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;
import com.github.javaparser.resolution.types.ResolvedPrimitiveType;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;
import com.microsoft.z3.*;

public class SMTConstraintGenerator extends GenericVisitorAdapter<Expr<? extends Sort>, Context> {

    public static class SMTConstraint {
        public final Expr<BoolSort> expr;
        public final List<Expr<BoolSort>> assertions;

        SMTConstraint(Expr<BoolSort> expr, List<Expr<BoolSort>> assertions) {
            this.expr = expr;
            this.assertions = assertions;
        }

    }

    public static SMTConstraint generateConstraints(Expression e, Context ctx) {
        SMTConstraintGenerator cg = new SMTConstraintGenerator();
        Expr<BoolSort> c =  (Expr<BoolSort>)e.accept(cg, ctx);
        return new SMTConstraint(c, cg.assertions);
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


    public Expr<? extends Sort> visit(UnaryExpr n, Context ctx) {
        Expr<? extends Sort> expr = n.getExpression().accept(this, ctx);
        switch (n.getOperator()) {
            case BITWISE_COMPLEMENT:
                break;
            case LOGICAL_COMPLEMENT:
                return ctx.mkNot((BoolExpr)expr);
            case MINUS:
                break;
            case PLUS:
                break;
            case POSTFIX_DECREMENT:
                break;
            case POSTFIX_INCREMENT:
                break;
            case PREFIX_DECREMENT:
                break;
            case PREFIX_INCREMENT:
                break;
            default:
                break;
        }
        throw new NotImplementedException();

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
                //  TODO: check for null explicitly, since this breaks types of Z3
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
    public Expr<? extends Sort> visit(MethodCallExpr n, Context ctx) {
        Expr<? extends Sort> scope = n.getScope().get().accept(this, ctx);

        List<Expr<? extends Sort>> args = new ArrayList<>();
        args.add(scope);

        for (Expression e : n.getArguments()) {
            args.add(e.accept(this, ctx));
        }

        Sort[] argSorts = new Sort[args.size()];
        for (int i = 0; i < args.size(); i++) {
            argSorts[i] = args.get(i).getSort();
        }

        String name = n.getName().asString();
        // Make a new function symbol applying 
        Sort returnSort = typeToSort(n.getData(DATA_KEY).type, ctx);
        FuncDecl<Sort> fnDecl = ctx.mkFuncDecl(name, argSorts, returnSort);
        return ctx.mkApp(fnDecl, args.toArray(new Expr[args.size()]));

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

    int counter = 0;
    public List<Expr<BoolSort>> assertions = new ArrayList<>();
    public Map<String, Expr<UninterpretedSort>> stringLiteralIntern = new HashMap<>();
    public Expr<? extends Sort> visit(StringLiteralExpr n, Context ctx ) {
        UninterpretedSort stringSort = ctx.mkUninterpretedSort("java.lang.String");
        if (stringLiteralIntern.containsKey(n.getValue())) {
            return stringLiteralIntern.get(n.getValue());
        }
        String name = "string_lit_" + counter++;
        Expr<UninterpretedSort> lit = ctx.mkConst(name, stringSort);
        stringLiteralIntern.put(n.getValue(), lit);
        FuncDecl<BitVecSort> fn_length = ctx.mkFuncDecl("java.lang.String.length", new Sort[] {stringSort}, ctx.mkBitVecSort(32));
        FuncDecl<BitVecSort> fn_charAt = ctx.mkFuncDecl("java.lang.String.charAt", new Sort[] {stringSort, ctx.mkBitVecSort(32)}, ctx.mkBitVecSort(16));
        assertions.add(ctx.mkEq(fn_length.apply(lit), ctx.mkBV(n.getValue().length(), 32)));
        for (int i = 0; i < n.getValue().length(); i++) {
            assertions.add(ctx.mkEq(fn_charAt.apply(lit, ctx.mkBV(i, 32)), ctx.mkBV(n.getValue().charAt(i), 16)));
        }

        return lit;
    }

    Map<String, Expr<UninterpretedSort>> nullLiteralIntern = new HashMap<>();
    @Override
    public Expr<? extends Sort> visit(NullLiteralExpr n, Context ctx) {
        ResolvedType resolvedType = n.getData(DATA_KEY).type;
        UninterpretedSort sort = (UninterpretedSort)typeToSort(resolvedType, ctx);
        if (resolvedType.isReferenceType()) {
            ResolvedReferenceType refType = resolvedType.asReferenceType();
            String qualName = refType.getQualifiedName();
            return nullLiteralIntern.computeIfAbsent(qualName, (k) -> ctx.mkConst(qualName + "_null", sort));
        } else if (resolvedType.isArray()) {
            throw new NotImplementedException("Array Types");
        }
        throw new IllegalStateException("Null must be a reference type");
    }

    /**
     * Get a Z3 sort for a given Java type
     * @param t
     * @param ctx
     * @return
     */
    private Sort typeToSort(ResolvedType t, Context ctx) { 
        if (t.isReferenceType()) {
            return ctx.mkUninterpretedSort(t.asReferenceType().getQualifiedName());
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
        } else if (t.isNull()) {
            throw new NotImplementedException("Null Type");
        }
        throw new IllegalStateException("Type " + t + ": Neither reference or primitive");
    }
    
}
