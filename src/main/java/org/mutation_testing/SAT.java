package org.mutation_testing;

import java.util.Map;

import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.resolution.types.ResolvedType;
import com.microsoft.z3.*;

public class SAT {

    boolean checkPredicate(Expression e, Map<NameExpr, ResolvedType> types) {
        Context ctx = new Context();
        Solver s = ctx.mkSolver();
        // TODO: Check the predicate

        ctx.close();
        return false;
    }
}
