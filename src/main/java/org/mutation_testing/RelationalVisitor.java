package org.mutation_testing;
import java.util.ArrayList;
import java.util.List;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

public class RelationalVisitor extends VoidVisitorAdapter<Void> {
    String currentMethod = null;
    List<MethodRelations> methodRelations = new ArrayList<>();
    MethodRelations mr = null;

    protected boolean isTerminal(Expression e) {
        return e.isNameExpr() || e.isLiteralExpr();
    }


    @Override
    public void visit(MethodDeclaration n, Void arg) {
        mr  = new MethodRelations(n);
        methodRelations.add(mr);
        System.out.println("Method: " + n.getNameAsString());
        super.visit(n, arg);
        System.out.println(mr.relations);
        mr = null;
    }

    @Override
    public void visit(BinaryExpr n, Void arg) {
        switch (n.getOperator()) {
            case EQUALS:
            case NOT_EQUALS:
            case GREATER:
            case GREATER_EQUALS:
            case LESS:
            case LESS_EQUALS:
                if (mr != null){
                    mr.relations.add(n);

                }
                break;
            default:
        }
        super.visit(n, arg);
    }

    @Override
    public void visit(Name n, Void arg) {
        System.out.println("Name: " + n);
        super.visit(n, arg);
    }

    @Override
    public void visit(NameExpr n, Void arg) {
        System.out.println("NameExpr: " + n);
        super.visit(n, arg);
    }

    class MethodRelations {
        final MethodDeclaration methodDeclaration;
        final String methodName;
        final List<Expression> relations;
        final List<SimpleName> parameters;
        final List<SimpleName> fields;

        public MethodRelations(MethodDeclaration md) {
            methodDeclaration = md;
            methodName = md.getName().toString();
            parameters = new ArrayList<>();
            md.getParameters().forEach(p -> parameters.add(p.getName()));
            relations = new ArrayList<>();
            fields = new ArrayList<>();
        }

        void addRelation(Expression e) {
            relations.add(e);
        }
    }

}
