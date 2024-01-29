package org.mutation_testing;

import java.util.ArrayList;
import java.util.List;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.SimpleName;

/**
 * The relations 
 */
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