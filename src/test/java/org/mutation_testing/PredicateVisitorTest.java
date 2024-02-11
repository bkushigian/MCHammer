package org.mutation_testing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.mutation_testing.predicates.ExprLiteralRelation;
import org.mutation_testing.predicates.ExprNameRelation;
import org.mutation_testing.predicates.NameLiteralRelation;
import org.mutation_testing.predicates.NameNameRelation;
import org.mutation_testing.predicates.Predicate;
import org.mutation_testing.predicates.Relation;
import org.mutation_testing.predicates.PredicateVisitor;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

public class PredicateVisitorTest {
    @Test
    public void testVisitMethodDecl() {
        String prog = "class A { boolean m(int x) { return x >= 32 && x < 127; } }";
        CompilationUnit cu = StaticJavaParser.parse(prog);
        List<Relation> relations = PredicateVisitor.collectPredicates(cu).stream()
                .map(p -> p.asRelation()).collect(Collectors.toList());
        assertEquals(2, relations.size());

        assertTrue(relations.get(0).isNameLiteralRelation());
        NameLiteralRelation r1 = relations.get(0).asNameLiteralRelation();
        assertNotNull(r1);
        assertTrue(r1.isOrdered());
        assertEquals("x", r1.getName().getNameAsString());
        IntegerLiteralExpr lit1 = r1.getLiteral().asIntegerLiteralExpr();
        assertEquals("32", lit1.getValue());

        assertTrue(relations.get(1).isNameLiteralRelation());
        NameLiteralRelation r2 = relations.get(1).asNameLiteralRelation();
        assertNotNull(r2);
        assertTrue(r2.isOrdered());
        assertEquals("x", r2.getName().getNameAsString());
        IntegerLiteralExpr lit2 = r2.getLiteral().asIntegerLiteralExpr();
        assertEquals("127", lit2.getValue());
    }

    @Test
    public void testVisitExpression01() {
        String expr = "x <= 32 || (x == 127 && y > 64) || (z <= 64 && y != 32)";
        Expression e = StaticJavaParser.parseExpression(expr);
        List<Relation> relations = PredicateVisitor.collectPredicates(e).stream()
                .map(p -> p.asRelation()).collect(Collectors.toList());

        assertEquals(5, relations.size());

        NameLiteralRelation r0 = relations.get(0).asNameLiteralRelation();
        assertNotNull(r0);
        assertTrue(r0.isOrdered());
        assertEquals("x", r0.getName().getNameAsString());
        IntegerLiteralExpr lit0 = r0.getLiteral().asIntegerLiteralExpr();
        assertEquals("32", lit0.getValue());

        NameLiteralRelation r1 = relations.get(1).asNameLiteralRelation();
        assertNotNull(r1);
        assertTrue(!r1.isOrdered());
        assertEquals("x", r1.getName().getNameAsString());
        IntegerLiteralExpr lit1 = r1.getLiteral().asIntegerLiteralExpr();
        assertEquals("127", lit1.getValue());

        NameLiteralRelation r2 = relations.get(2).asNameLiteralRelation();
        assertNotNull(r2);
        assertTrue(r2.isOrdered());
        assertEquals("y", r2.getName().getNameAsString());
        IntegerLiteralExpr lit2 = r2.getLiteral().asIntegerLiteralExpr();
        assertEquals("64", lit2.getValue());

        NameLiteralRelation r3 = relations.get(3).asNameLiteralRelation();
        assertNotNull(r3);
        assertTrue(r3.isOrdered());
        assertEquals("z", r3.getName().getNameAsString());
        IntegerLiteralExpr lit3 = r3.getLiteral().asIntegerLiteralExpr();
        assertEquals("64", lit3.getValue());

        NameLiteralRelation r4 = relations.get(4).asNameLiteralRelation();
        assertNotNull(r4);
        assertTrue(!r4.isOrdered());
        assertEquals("y", r4.getName().getNameAsString());
        IntegerLiteralExpr lit4 = r4.getLiteral().asIntegerLiteralExpr();
        assertEquals("32", lit4.getValue());
    }

    @Test
    public void testVisitExpression02() {
        String expr = "x <= y && y == z || x*x <= 32 || x == y * y";
        Expression e = StaticJavaParser.parseExpression(expr);
        List<Relation> relations = PredicateVisitor.collectPredicates(e).stream()
                .map(p -> p.asRelation()).collect(Collectors.toList());
        assertEquals(4, relations.size());

        NameNameRelation r0 = relations.get(0).asNameNameRelation();
        assertNotNull(r0);
        assertTrue(r0.isOrdered());
        assertEquals("x", r0.getName1().getNameAsString());
        assertEquals("y", r0.getName2().getNameAsString());

        NameNameRelation r1 = relations.get(1).asNameNameRelation();
        assertNotNull(r1);
        assertTrue(!r1.isOrdered());
        assertEquals("y", r1.getName1().getNameAsString());
        assertEquals("z", r1.getName2().getNameAsString());

        ExprLiteralRelation r2 = relations.get(2).asExprLiteralRelation();
        assertNotNull(r2);
        assertTrue(r2.isOrdered());
        assertEquals("x*x", r2.getExpr().toString().replace(" ", ""));
        IntegerLiteralExpr lit2 = r2.getLiteral().asIntegerLiteralExpr();
        assertEquals("32", lit2.getValue());

        ExprNameRelation r3 = relations.get(3).asExprNameRelation();
        assertNotNull(r3);
        assertTrue(!r3.isOrdered());
        assertEquals("x", r3.getName().toString());
        assertEquals("y*y", r3.getExpr().toString().replace(" ", ""));
    }

    @Test
    public void testCollectMethodPredicate() {
        String prog = "class A {" +
                "   boolean p(String s) {" +
                "       return s.startsWith(\"a\") && s.length() < 32;" +
                "   }" +
                "}";
        CompilationUnit cu = StaticJavaParser.parse(prog);
        MethodDeclaration md = cu.findFirst(MethodDeclaration.class).get();
        List<Predicate> predicates = PredicateVisitor.collectPredicates(md);
        assertEquals(2, predicates.size());
        assertTrue(predicates.get(0).isMethodCallPredicate());
        assertTrue(predicates.get(1).isRelation());
        assertTrue(predicates.get(1).asRelation().isExprLiteralRelation());
        
    }

    @Before
    public void setUp() throws Exception {
        CombinedTypeSolver typeSolver = new CombinedTypeSolver();
        typeSolver.add(new ReflectionTypeSolver());
        StaticJavaParser.getParserConfiguration().setSymbolResolver(new JavaSymbolSolver(typeSolver));
    }

}
