package org.mutation_testing;

import java.util.ArrayList;
import java.util.List;

import com.github.javaparser.ast.expr.Expression;

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
public class MCs {

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

    public MCs refine(MCs refinement) {
        return new Refine(this, refinement);
    }

    public static MCs join(MCs... conditions) {
        List<MCs> list = new ArrayList<>();
        for (MCs c : conditions) {
            list.add(c);
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
    }

    public static class False extends MCs {
        private False() {
            super(MutationConditionType.FALSE);
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
    }

    public static class Refine extends MCs {
        public final MCs condition;
        public final MCs refinement;

        Refine(MCs condition, MCs refinement) {
            super(MutationConditionType.REFINE);
            this.condition = condition;
            this.refinement = refinement;
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
    }
}
