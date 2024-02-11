package org.mutation_testing.predicates;

import java.util.ArrayList;
import java.util.List;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

/**
 * A visitor to collect all relations within a tree.
 */
public class PredicateVisitor extends VoidVisitorAdapter<List<Predicate>> {
    String currentMethod = null;

    public List<Predicate> collectRelations(Node node) {
        List<Predicate> predicates = new ArrayList<>();
        node.accept(this, predicates);
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
}
