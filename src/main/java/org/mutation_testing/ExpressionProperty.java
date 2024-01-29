package org.mutation_testing;

import com.github.javaparser.ast.DataKey;
import com.github.javaparser.ast.expr.ArrayAccessExpr;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.ConditionalExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.LiteralExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.UnaryExpr;


public abstract class ExpressionProperty implements Property {
   final static DataKey<ExpressionProperty> key = new DataKey<ExpressionProperty>() {};
   final protected Expression e; 
   final static protected String propertyName = "ExpressionProperty(abstract)";

    protected ExpressionProperty(Expression e) {
        this.e = e;
    }

    public <T extends ExpressionProperty> T join(Expression e){
        if (e.isLiteralExpr()) {
            return fromLiteralExpression(e.asLiteralExpr());
        }
        if (e.isNameExpr()) {
            return fromNameExpression(e.asNameExpr());
        }
        if (e.isAssignExpr()) {
            return fromAssignExpression(e.asAssignExpr());
        }
        if (e.isArrayAccessExpr()) {
            return fromArrayAccessExpression(e.asArrayAccessExpr());
        }
        throw new IllegalStateException("Unhandled expression type: " + e.getClass().getName());
    };


    public abstract <T extends ExpressionProperty> T join(BinaryExpr e, ExpressionProperty left, ExpressionProperty right);
    public abstract <T extends ExpressionProperty> T join(UnaryExpr e, ExpressionProperty op);
    public abstract <T extends ExpressionProperty> T join(MethodCallExpr e, ExpressionProperty...args);
    public abstract <T extends ExpressionProperty> T join(ConditionalExpr e, ExpressionProperty cond, ExpressionProperty thn, ExpressionProperty els);
    public abstract <T extends ExpressionProperty> T join(ArrayAccessExpr e, ExpressionProperty target, ExpressionProperty access);

    public abstract <T extends ExpressionProperty> T fromLiteralExpression(LiteralExpr e);
    public abstract <T extends ExpressionProperty> T fromNameExpression(NameExpr e);
    public abstract <T extends ExpressionProperty> T fromAssignExpression(AssignExpr e);

    public abstract <T extends ExpressionProperty> T fromArrayAccessExpression(ArrayAccessExpr e);



    public Expression getExpression() {
         return e;
    }

    public String getPropertyName() {
        return propertyName;
    }
}
