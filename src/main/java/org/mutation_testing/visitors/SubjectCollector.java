package org.mutation_testing.visitors;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ThisExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;

/**
 * Traverse an expression and collect all the 'subjects' of the expression. A
 * subject is
 * defined to be:
 * - A variable
 * - A field access
 * - The scope of a method call (if non-null)
 * 
 * This is useful for determing what a predicate is <i>about</i>; that is, what
 * objects in code correspond
 * 
 */
public class SubjectCollector extends VoidVisitorAdapter<Collection<Expression>> {

    public static Set<Expression> collectSubjects(Node expr) {
        Set<Expression> subjects = new HashSet<>();
        expr.accept(new SubjectCollector(), subjects);
        return subjects;
    }

    @Override
    public void visit(NameExpr n, Collection<Expression> subjects) {
        subjects.add(n);
    }

    @Override
    public void visit(FieldAccessExpr n, Collection<Expression> subjects) {
        subjects.add(n);
    }

    @Override
    public void visit(ThisExpr n, Collection<Expression> subjects) {
        subjects.add(n);
    }

    @Override
    public void visit(MethodCallExpr n, Collection<Expression> subjects) {
        /*
         * We start by visiting scope. We need to resolve to figure out if this
         * is a static method call or not. If it is we don't add the scope to
         * the list of subjects.
         * 
         * Once we've handled the scope we check the arguments for subjects.
         */
        try {
            // Resolve the method and check if static. If so, we don't need to
            // add the scope. Otherwise, we want to infer the scope of this
            // method. If it is not present then it is the current object;
            // otherwise, we add the explicitly stated scope.
            ResolvedMethodDeclaration m = n.resolve();
            if (!m.isStatic()) {
                if (n.getScope().isPresent()) {
                    n.getScope().get().accept(this, subjects);
                } else {
                    subjects.add(new ThisExpr());
                }
            }
        } catch (Exception e) {
            // By default assume this is a non-static method call
            subjects.add(new ThisExpr());
        }
        super.visit(n.getArguments(), subjects);
    }

}
