package org.mutation_testing;


public class AbstractStateVisitor {
    AbstractStates visitExpr() {
        return new AbstractStates();
    }
}
