package org.mutation_testing.predicates;

/**
 * Represents a boolean expression found in code, or one that can be built from
 * the syntax tree to represent an interesting condition about the program.
 * {@code Predicate}s are used to represent Mutation Conditions.
 * 
 */
public class Predicate {

    public boolean isMethodCallPredicate() {
        return false;
    }

    public MethodCallPredicate asMethodCallPredicate() {
        throw new UnsupportedOperationException("Predicate is not a method predicate");
    }

    public boolean isRelation() {
        return false;
    }

    public Relation asRelation() {
        throw new UnsupportedOperationException("Predicate is not a relation");
    }

    public boolean isNamePredicate() {
        return false;
    }

    public NamePredicate asNamePredicate() {
        throw new UnsupportedOperationException("Predicate is not a name predicate");
    }
}
