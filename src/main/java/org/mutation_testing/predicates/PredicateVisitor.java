package org.mutation_testing.predicates;

import java.util.ArrayList;
import java.util.List;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.types.ResolvedType;

/**
 * A visitor to collect all relations within a tree.
 * 
 * This visitor performs Symbol Resolution and a symbol solver must be set up
 * before parsing.
 */
public class PredicateVisitor extends VoidVisitorAdapter<List<Predicate>> {
    String currentMethod = null;

    public static List<Predicate> collectPredicates(Node n) {
        List<Predicate> predicates = new ArrayList<>();
        n.accept(new PredicateVisitor(), predicates);
        return predicates;
    }


    @Override
    public void visit(BinaryExpr n, List<Predicate> arg) {
        switch (n.getOperator()) {
            case EQUALS:
            case NOT_EQUALS:
            case GREATER:
            case GREATER_EQUALS:
            case LESS:
            case LESS_EQUALS:
                if (arg != null) {
                    arg.add(Relation.from(n));
                }
                return;
            default:
        }
        super.visit(n, arg);
    }

    @Override
    public void visit(MethodCallExpr n, List<Predicate> arg) {
        if (isBooleanType(n.resolve().getReturnType())) {
            arg.add(new MethodCallPredicate(n));
            return;
        }

        super.visit(n, arg);
    }

    @Override
    public void visit(NameExpr n, List<Predicate> arg) {
        if (isBooleanType(n.resolve().getType())) {
            arg.add(new NamePredicate(n));
            return;
        }

        super.visit(n, arg);
    }

    boolean isBooleanType(ResolvedType tp) {
        return tp.isPrimitive() && tp.asPrimitive().isBoolean()
                || tp.isReferenceType() && tp.asReferenceType().getQualifiedName().equals("java.lang.Boolean");
    }
}
