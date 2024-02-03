package org.mutation_testing.state;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;
import org.mutation_testing.relation.RelationalVisitor;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.expr.Expression;

public class StoreTest {
    RelationalVisitor rv = new RelationalVisitor();

    protected Store storeFromExpr(Expression expr) {
        return new Store(rv.collectRelations(expr));
    }

    @Test
    public void testStoreProductForSingleVariable() {
        // This should produce 4 abstract states
        // 1. x <= 4 && x != 1
        // 2. x == 1
        // 3. x == 5
        // 4. x >= 6
        String exprString = "x == 1 || x > 5";
        Store store =  storeFromExpr(StaticJavaParser.parseExpression(exprString));
        assertTrue(store.fieldStore.isEmpty());
        assertTrue(store.miscStore.isEmpty());

        assertEquals(1, store.localStore.size());
        assertTrue(store.localStore.containsKey("x"));

        List<Expression> product = store.getProductConditions();
        System.out.println("Product: " + product);
        assertEquals(4, product.size());

    }

    @Test
    public void testStoreProductForTwoVariables01() {
        // This should produce 4 abstract states: 2 for x, 2 for y

        String exprString = "x == 1 && y != 1";
        Store store =  storeFromExpr(StaticJavaParser.parseExpression(exprString));

        List<Expression> product = store.getProductConditions();
        assertEquals(4, product.size());

    }
    @Test 
    public void testStoreProductForTwoVariables02() {
        // This should produce 9 abstract states: (the product of 3 for x, 3 for y)
        String exprString = "x == 1 || x == 2 || y == 1 || y == 2";
        Store store =  storeFromExpr(StaticJavaParser.parseExpression(exprString));

        List<Expression> product = store.getProductConditions();
        assertEquals(9, product.size());

    }


    
}
