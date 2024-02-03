package org.mutation_testing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;


import org.junit.Test;
import org.mutation_testing.relation.ExprLiteralRelation;
import org.mutation_testing.relation.ExprNameRelation;
import org.mutation_testing.relation.NameLiteralRelation;
import org.mutation_testing.relation.NameNameRelation;
import org.mutation_testing.relation.Relation;
import org.mutation_testing.relation.RelationalVisitor;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;

public class RelationalVisitorTest {
    @Test
    public void testVisitMethodDecl() {
        String prog = "class A { boolean m(int x) { return x >= 32 && x < 127; } }";
        CompilationUnit cu = StaticJavaParser.parse(prog);
        RelationalVisitor v = new RelationalVisitor();
        List<Relation> relations = v.collectRelations(cu);
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
        RelationalVisitor v = new RelationalVisitor();
        List<Relation> relations = v.collectRelations(e);

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
    public void testVisiteExpression02() {
        String expr = "x <= y && y == z || x*x <= 32 || x == y * y";
        Expression e = StaticJavaParser.parseExpression(expr);
        RelationalVisitor v = new RelationalVisitor();
        List<Relation> relations = v.collectRelations(e);
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

}
