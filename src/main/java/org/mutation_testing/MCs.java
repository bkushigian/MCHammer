package org.mutation_testing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

import org.mutation_testing.smt.SMTConstraintGenerator;
import static org.mutation_testing.ExprUtils.and;

import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.Expression;
import com.microsoft.z3.BoolSort;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Sort;
import com.microsoft.z3.Status;

/**
 * Mutation Conditions
 * 
 * This class represents partitions of mutation conditions in an algebraic
 * format. There are 5 types of MCs: TRUE, FALSE, JOIN, REFINE, PREDICATES.
 * 
 * <ol>
 * <li>
 * <b>TRUE</b>: the condition that is always true, e.g., at the top of a public
 * method
 * </li>
 * <li>
 * <b>FALSE</b>: the condition that is always false, e.g., after a return
 * </li>
 * <li>
 * <b>JOIN</b>: the join of multiple conditions, e.g., after a branch
 * </li>
 * <li>
 * <b>REFINE</b>: the refinement of one MCs by another.
 * </li>
 * <li>
 * <b>PREDICATES</b>: an explicit set of predicates, e.g., the set <code>{a &lt;
 * b, a = b, a > b}</code> generated when visiting the expression <code> a &lt;
 * b </code>
 * </li>
 * 
 *
 */
public abstract class MCs {

    /**
     * Should we optimize the MCs when we create them?
     */
    static boolean optimize = false;

    public static void setOptimize(boolean optimize) {
        MCs.optimize = optimize;
    }

    public static enum MutationConditionType {
        TRUE, FALSE, JOIN, REFINE, PREDICATES
    }

    public final MutationConditionType type;

    protected MCs(MutationConditionType type) {
        this.type = type;
    }

    public boolean isTrue() {
        return type == MutationConditionType.TRUE;
    }

    public boolean isFalse() {
        return type == MutationConditionType.FALSE;
    }

    public boolean isJoin() {
        return type == MutationConditionType.JOIN;
    }

    public boolean isRefine() {
        return type == MutationConditionType.REFINE;
    }

    public boolean isPredicates() {
        return type == MutationConditionType.PREDICATES;
    }

    public abstract List<Expression> toConditions();

    public List<Expression> toSATConditions() {
        Context ctx = new Context();
        Solver solver = ctx.mkSolver();
        List<Expression> result = new ArrayList<>();
        List <Expression> conditions = toConditions();
        List<Expression> simplifiedConditions = new ArrayList<>();
        for (Expression condition : conditions) {
            Expression simplifiedCondition = MCsOptimizer.simplifyConjunction(condition);
            simplifiedConditions.add(simplifiedCondition);
        }
        for (Expression e : simplifiedConditions) {
            solver.push();

            Expr<? extends Sort> constraints = SMTConstraintGenerator.generateConstraints(e, ctx);
            solver.add((Expr<BoolSort>)constraints);
            Status status = solver.check();
            if (status == Status.SATISFIABLE){
                result.add(e);
            }
            solver.pop();
        }
        ctx.close();
        return result;
    }

    public MCs refine(MCs refinement) {
        if (optimize) {
            if (this.isTrue())
                return refinement;
            if (this.isFalse())
                return this;
            if (refinement.isTrue())
                return this;
            if (refinement.isFalse())
                return refinement;
            if (this.equals(refinement))
                return this;
        }
        return new Refine(this, refinement);
    }

    public static MCs join(MCs... conditions) {
        if (conditions.length == 0) {
            return FALSE;
        }
        List<MCs> list = new ArrayList<>();
        for (MCs c : conditions) {
            if (optimize) {
                if (c.isTrue())
                    return TRUE;
                if (c.isFalse())
                    continue;
            }
            list.add(c);
        }
        if (list.isEmpty()) {
            return FALSE;
        }
        return new Join(list);
    }

    public static MCs join(Collection<MCs> conditions) {
        List<MCs> list = new ArrayList<>();
        for (MCs c : conditions) {
            if (optimize) {
                if (c.isTrue())
                    return TRUE;
                if (c.isFalse())
                    continue;
            }
            list.add(c);
        }
        if (list.isEmpty()) {
            return FALSE;
        }
        return new Join(list);
    }

    public static final MCs TRUE = new True();
    public static final MCs FALSE = new False();

    public static MCs.Predicates predicates(List<Expression> predicates) {
        return new Predicates(predicates);
    }

    public static MCs.Predicates predicates(Expression... predicates) {
        return new Predicates(predicates);
    }

    public static class True extends MCs {
        private True() {
            super(MutationConditionType.TRUE);
        }

        @Override
        public String toString() {
            return "TRUE";
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof True;
        }

        @Override
        public List<Expression> toConditions() {
            List<Expression> result = new ArrayList<>();
            result.add(new BooleanLiteralExpr(true));
            return result;
        }
    }

    public static class False extends MCs {
        private False() {
            super(MutationConditionType.FALSE);
        }

        @Override
        public String toString() {
            return "FALSE";
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof False;
        }

        @Override
        public List<Expression> toConditions() {
            List<Expression> result = new ArrayList<>();
            result.add(new BooleanLiteralExpr(false));
            return result;
        }
    }

    public static class Join extends MCs {
        List<MCs> conditions = new ArrayList<>();

        Join(List<MCs> conditions) {
            super(MutationConditionType.JOIN);
            this.conditions = conditions;
        }

        Join(MCs... conditions) {
            super(MutationConditionType.JOIN);
            for (MCs c : conditions) {
                this.conditions.add(c);
            }
        }

        @Override
        public String toString() {
            StringJoiner sj = new StringJoiner(" ⊕ ");
            for (MCs c : conditions) {
                sj.add(c.toString());
            }
            return sj.toString();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Join) {
                Join other = (Join) obj;
                return new HashSet<>(conditions).equals(new HashSet<>(other.conditions));
            }
            return false;
        }

        @Override
        public int hashCode() {
            return new HashSet<>(conditions).hashCode();
        }

        @Override
        public List<Expression> toConditions() {
            List<Expression> result = new ArrayList<>();
            for (MCs c : conditions) {
                result.addAll(c.toConditions());
            }
            return result;
        }
    }

    public static class Refine extends MCs {
        public final MCs condition;
        public final MCs refinement;

        Refine(MCs condition, MCs refinement) {
            super(MutationConditionType.REFINE);
            this.condition = condition;
            this.refinement = refinement;
        }

        @Override
        public String toString() {
            String cString = condition.toString();
            String rString = refinement.toString();
            if (condition.isJoin()) {
                cString = "(" + cString + ")";
            }
            if (refinement.isJoin()) {
                rString = "(" + rString + ")";
            }
            return cString + "⊗" + rString;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Refine) {
                Refine other = (Refine) obj;
                return condition.equals(other.condition) && refinement.equals(other.refinement);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return condition.hashCode() + refinement.hashCode();
        }

        @Override
        public List<Expression> toConditions() {
            List<Expression> result = new ArrayList<>();
            List<Expression> lhs = condition.toConditions();
            List<Expression> rhs = refinement.toConditions();

            for (Expression l : lhs) {
                for (Expression r : rhs) {
                    result.add(and(l, r));
                }
            }

            return result;
        }
    }

    public static class Predicates extends MCs {
        public final List<Expression> predicates;

        Predicates(List<Expression> predicates) {
            super(MutationConditionType.PREDICATES);
            this.predicates = new ArrayList<>(predicates);
        }

        Predicates(Expression... predicates) {
            super(MutationConditionType.PREDICATES);
            this.predicates = new ArrayList<>();
            for (Expression e : predicates) {
                this.predicates.add(e);
            }
        }

        @Override
        public String toString() {
            StringJoiner sj = new StringJoiner(", ");
            for (Expression e : predicates) {
                sj.add(e.toString());
            }
            return "{" + sj.toString() + "}";
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Predicates) {
                Predicates other = (Predicates) obj;
                return new HashSet<>(predicates).equals(new HashSet<>(other.predicates));
            }
            return false;
        }

        @Override
        public int hashCode() {
            return new HashSet<>(predicates).hashCode();
        }

        @Override
        public List<Expression> toConditions() {
            return new ArrayList<>(predicates);
        }
    }

    public static MCs optimize(MCs mcs) {
        if (mcs.isTrue() || mcs.isFalse() || mcs.isPredicates()) {
            return mcs;
        }

        if (mcs.isRefine()) {
            List<MCs> flattened = flatRefineOperands(mcs);
            Set<MCs> unique = new HashSet<>(flattened);
            List<MCs> ops = new ArrayList<>(unique);
            MCs result = TRUE;
            for (MCs op : ops) {
                result = result.refine(op);
            }
            return result;
        } else if (mcs.isJoin()) {
            return MCs.join(new HashSet<>(flatJoinOperands(mcs)));
        }
        return mcs;
    }

    static List<MCs> flatRefineOperands(MCs mcs) {
        List<MCs> result = new ArrayList<>();
        if (mcs.isRefine()) {
            Refine r = (Refine) mcs;
            result.addAll(flatRefineOperands(r.condition));
            result.addAll(flatRefineOperands(r.refinement));
        } else {
            result.add(mcs);
        }
        return result;
    }

    static List<MCs> flatJoinOperands(MCs mcs) {
        List<MCs> result = new ArrayList<>();
        if (mcs.isJoin()) {
            Join r = (Join) mcs;
            for (MCs c : r.conditions) {
                result.addAll(flatJoinOperands(c));
            }
        } else {
            result.add(mcs);
        }
        return result;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MCs other = (MCs) obj;
        if (type != other.type)
            return false;
        return true;
    }
}
