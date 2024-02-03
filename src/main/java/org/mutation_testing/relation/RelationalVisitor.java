package org.mutation_testing.relation;

import java.util.ArrayList;
import java.util.List;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

/**
 * A visitor to collect all relations within a tree.
 */
public class RelationalVisitor extends VoidVisitorAdapter<List<Relation>> {
    String currentMethod = null;

    public List<Relation> collectRelations(Node node) {
        List<Relation> relations = new ArrayList<>();
        node.accept(this, relations);
        return relations;
    }


    @Override
    public void visit(BinaryExpr n, List<Relation> arg) {
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
