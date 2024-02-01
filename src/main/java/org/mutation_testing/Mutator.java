package org.mutation_testing;

import java.util.ArrayList;
import java.util.List;

import org.mutation_testing.ExpressionPropertyVisitor.Properties;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.CallableDeclaration.Signature;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.CharLiteralExpr;
import com.github.javaparser.ast.expr.ConditionalExpr;
import com.github.javaparser.ast.expr.DoubleLiteralExpr;
import com.github.javaparser.ast.expr.EnclosedExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.LongLiteralExpr;
import com.github.javaparser.ast.expr.UnaryExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.TypeSolver;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;

public class Mutator extends VoidVisitorAdapter<Void> {
    protected int mid = 1;
    protected Source source;

    protected ExpressionPropertyVisitor epv = new ExpressionPropertyVisitor();
    protected RelationalVisitor rv = new RelationalVisitor();

    protected TypeSolver typeSolver = new CombinedTypeSolver();
    protected JavaSymbolSolver symbolSolver = new JavaSymbolSolver(typeSolver);

    public Mutator() {
        StaticJavaParser.getParserConfiguration().setSymbolResolver(symbolSolver);
    }

    /**
     * Mutants for the current file
     */
    protected List<Mutant> mutants = new ArrayList<>();

    protected boolean ready() {
        return source == null;
    }

    protected void cleanup() {
        source = null;
        mutants = null;
    }

    public List<Mutant> mutateFile(String filename) throws IOException {
        System.out.println("Mutating file " + filename);
        String fileContents = new String(Files.readAllBytes(Paths.get(filename)));
        return mutate(filename, fileContents);
    }

    public List<Mutant> mutate(String filename, String fileContents) {
        if (!ready()) {
            throw new IllegalStateException("Cannot mutate file while mutating another file");
        }

        this.source = new Source(filename, fileContents);
        mutants = new ArrayList<>();

        CompilationUnit cu = StaticJavaParser.parse(fileContents);
        cu.accept(this, null);

        List<Mutant> result = mutants;
        cleanup();

        return result;
    }

    protected Expression getInfectingExpression(Expression e) {
        System.out.println("Calculating type...");
        ResolvedType type = e.calculateResolvedType();
        Expression clonedExpr = new EnclosedExpr(e.clone());
        System.out.println("  TYPE: " + type.describe());
        if (type.isPrimitive()) {

            switch (type.asPrimitive()) {
                case BOOLEAN:
                    return new UnaryExpr(clonedExpr, UnaryExpr.Operator.LOGICAL_COMPLEMENT);
                case CHAR:
                    return new BinaryExpr(clonedExpr, new CharLiteralExpr((char) 97), BinaryExpr.Operator.PLUS);
                case BYTE:
                case SHORT:
                case INT:
                    return new BinaryExpr(clonedExpr, new IntegerLiteralExpr("97"), BinaryExpr.Operator.PLUS);
                case LONG:
                    return new BinaryExpr(clonedExpr, new LongLiteralExpr("97"), BinaryExpr.Operator.PLUS);
                case FLOAT:
                case DOUBLE:
                    return new BinaryExpr(clonedExpr, new DoubleLiteralExpr("11.0"), BinaryExpr.Operator.PLUS);
                default:
                    throw new IllegalArgumentException("Unhandled primitive type " + type);
            }
        }
        return null;
    }

    protected void addMutantFromCondition(Expression originalExpr, Expression mutationCondition) {
        Expression mutatedExpr = getInfectingExpression(originalExpr);
        Expression clonedOriginalExpr = new EnclosedExpr(originalExpr.clone());
        clonedOriginalExpr.setParentNode(null);
        if (!originalExpr.getParentNode().isPresent()) {
            System.out.println("ERROR");
        }
        Expression repl = new ConditionalExpr(new EnclosedExpr(mutationCondition), mutatedExpr, clonedOriginalExpr);
        repl = new EnclosedExpr(repl);
        addMutant(originalExpr, repl);
    }

    protected void addMutant(Expression orig, Expression repl) {
        Mutant mutant = new Mutant(mid, source, orig, repl);
        mutants.add(mutant);
        mid += 1;
    }

    protected Signature signature;
    NodeList<Parameter> parameters;

    @Override
    public void visit(MethodDeclaration n, Void arg) {
        if (signature != null) {
            throw new IllegalStateException("nested methods");
        }
        signature = n.getSignature();
        parameters = n.getParameters();
        int midOld = mid;
        super.visit(n, arg);
        signature = null;
        parameters = null;
        int midNew = mid;
        System.out.println("MUTATED " + (midNew - midOld) + " mutants for method " + n.getName());
    }

    @Override
    public void visit(BinaryExpr n, Void arg) {
        // The fact that we are visiting means that there is no enclosing
        // expression that has already been mutated.

        System.out.println("MUTATING: " + n.toString());
        Properties ps = n.accept(epv, null);
        if (ps != null) {
            System.out.println("  PROPERTIES:" + ps.summarizeProperties());
            System.out.println("  CAN MUTATE? " + ps.canMutate());
        }

        if (ps == null || !ps.canMutate()) {
            System.out.println("  Recursively visiting children");
            super.visit(n, arg);
            return;
        }
        assert ps.isPure;
        assert !ps.hasUnhandledProperties;

        List<Expression> relations = new ArrayList<>();
        n.accept(rv, relations);
        System.out.println(relations);

        for (Expression r : relations) {
            System.out.println("  TARGETING MUTATION CONDITION: " + r);
            addMutantFromCondition(n, r);
        }
    }
}
