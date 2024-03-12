package org.mutation_testing;

import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.CastExpr;
import com.github.javaparser.ast.expr.CharLiteralExpr;
import com.github.javaparser.ast.expr.DoubleLiteralExpr;
import com.github.javaparser.ast.expr.EnclosedExpr;
import com.github.javaparser.ast.expr.InstanceOfExpr;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.LongLiteralExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.TextBlockLiteralExpr;
import com.github.javaparser.ast.expr.ThisExpr;
import com.github.javaparser.ast.expr.TypeExpr;
import com.github.javaparser.ast.expr.UnaryExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

public class DataVisitor extends VoidVisitorAdapter<Void> {

    @Override
    public void visit(BinaryExpr n, Void arg) {
        super.visit(n, arg);
        ExpressionData.applyToExpression(n);
    }

    @Override
    public void visit(UnaryExpr n, Void arg) {
        super.visit(n, arg);
        ExpressionData.applyToExpression(n);
    }

    @Override
    public void visit(ThisExpr n, Void arg) {
        super.visit(n, arg);
        ExpressionData.applyToExpression(n);
    }

    @Override
    public void visit(NameExpr n, Void arg) {
        super.visit(n, arg);
        ExpressionData.applyToExpression(n);
    }

    @Override
    public void visit(TypeExpr n, Void arg) {
        super.visit(n, arg);
        ExpressionData.applyToExpression(n);
    }

    @Override
    public void visit(AssignExpr n, Void arg) {
        super.visit(n, arg);
        ExpressionData.applyToExpression(n);
    }

    @Override
    public void visit(InstanceOfExpr n, Void arg) {
        super.visit(n, arg);
        ExpressionData.applyToExpression(n);
    }

    @Override
    public void visit(CastExpr n, Void arg) {
        super.visit(n, arg);
        ExpressionData.applyToExpression(n);
    }

    @Override
    public void visit(MethodCallExpr n, Void arg) {
        super.visit(n, arg);
        ExpressionData.applyToExpression(n);
    }

    @Override
    public void visit(EnclosedExpr n, Void arg) {
        super.visit(n, arg);
        ExpressionData.applyToExpression(n);
    }

    @Override
    public void visit(NullLiteralExpr n, Void arg) {
        super.visit(n, arg);
        ExpressionData.applyToExpression(n);
    }

    @Override
    public void visit(LongLiteralExpr n, Void arg) {
        super.visit(n, arg);
        ExpressionData.applyToExpression(n);
    }

    @Override
    public void visit(CharLiteralExpr n, Void arg) {
        super.visit(n, arg);
        ExpressionData.applyToExpression(n);

    }

    @Override
    public void visit(StringLiteralExpr n, Void arg) {
        super.visit(n, arg);
        ExpressionData.applyToExpression(n);
    }

    @Override
    public void visit(DoubleLiteralExpr n, Void arg) {
        super.visit(n, arg);
        ExpressionData.applyToExpression(n);
    }

    @Override
    public void visit(IntegerLiteralExpr n, Void arg) {
        super.visit(n, arg);
        ExpressionData.applyToExpression(n);
    }

    @Override
    public void visit(BooleanLiteralExpr n, Void arg) {
        super.visit(n, arg);
        ExpressionData.applyToExpression(n);
    }

    @Override
    public void visit(TextBlockLiteralExpr n, Void arg) {
        super.visit(n, arg);
        ExpressionData.applyToExpression(n);
    }
    
}
