package org.mutation_testing;

import java.util.List;
import java.util.Map;

import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.SimpleName;

/**
 * This stores abstract values for parameters, fields, local variables, etc
 */
public class Store {
    /**
     * Map field names to a list of expressions that represent the possible
     */
    Map<Name, List<Expression>> fieldStore;
    Map<SimpleName, List<Expression>> localStore;

    /**
     * Each list of expressions should be boolean expressions such that
     * precisely one is true at any given time. This is not checked and must be
     * maintained by the developer.
     */
    List<List<Expression>> miscStore;

}
