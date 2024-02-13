package org.mutation_testing.visitors;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

/**
 * Traverse an expression and collect all the 'subjects' of the expression. A subject is
 * defined to be:
 * - A variable
 * - A field access
 * - The scope of a method call (if non-null)
 * - A constructor call
 * 
 */
public class PredicateSubjectVisitor extends VoidVisitorAdapter<Collection<Expression>> {

    public static Set<Expression> collectSubjects(Expression expr) {
        Set<Expression> subjects = new HashSet<>();
        expr.accept(new PredicateSubjectVisitor(), subjects);
        return subjects;
    }

    @Override
    public void visit(NameExpr n, Collection<Expression> arg) {
        arg.add(n);
    }

    @Override
    public void visit(MethodCallExpr n, Collection<Expression> arg) {
        if (n.getScope().isPresent()) {
            arg.add(n.getScope().get());
        }
        // Visit args
        super.visit(n, arg);
    }
    
}
