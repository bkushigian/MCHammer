package org.mutation_testing;

import java.util.HashSet;
import java.util.Set;

import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

public class NameVisitor extends VoidVisitorAdapter<NameVisitor.UsedNames> {

    public static class UsedNames {
        Set<Name> usedNames = new HashSet<>();
        Set<SimpleName> usedSimpleNames = new HashSet<>();
    }

    public void visit(Name n, UsedNames arg) {
        arg.usedNames.add(n);
        super.visit(n, arg);
    }

    public void visit(NameExpr n, UsedNames arg) {
        arg.usedSimpleNames.add(n.getName());
        super.visit(n, arg);
    }

}
