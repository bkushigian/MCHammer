package org.mutation_testing.state;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mutation_testing.TestUtils;
import org.mutation_testing.predicates.PredicateVisitor;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

public class StoreTest {
    PredicateVisitor rv = new PredicateVisitor();

    @Before
    public void setUp() throws Exception {
        CombinedTypeSolver typeSolver = new CombinedTypeSolver();
        typeSolver.add(new ReflectionTypeSolver());
        StaticJavaParser.getParserConfiguration().setSymbolResolver(new JavaSymbolSolver(typeSolver));
    }

    @Test
    public void testStoreProductForSingleVariable() {
        // This should produce 4 abstract states
        // 1. x <= 4 && x != 1
        // 2. x == 1
        // 3. x == 5
        // 4. x >= 6
        String exprString = "x == 1 || x > 5";
        Store store = storeFromExpr(exprString, "int x");
        assertTrue(store.fieldStore.isEmpty());
        assertTrue(store.miscStore.isEmpty());

        assertEquals(1, store.localStore.size());
        assertTrue(store.localStore.containsKey("x"));

        List<Expression> product = store.getProductConditions();
        printProduct(exprString, store, product);
        assertEquals(4, product.size());

    }

    @Test
    public void testStoreProductForTwoVariables01() {
        // This should produce 4 abstract states: 2 for x, 2 for y

        String exprString = "x == 1 && y != 1";
        Store store = storeFromExpr(exprString, "int x", "int y");

        List<Expression> product = store.getProductConditions();
        printProduct(exprString, store, product);
        assertEquals(4, product.size());

    }

    @Test
    public void testStoreProductForTwoVariables02() {
        // This should produce 9 abstract states: (the product of 3 for x, 3 for y)
        String exprString = "x == 1 || x == 2 || y == 1 || y == 2";
        Store store = storeFromExpr(exprString, "int x", "int y");

        List<Expression> product = store.getProductConditions();
        printProduct(exprString, store, product);
        assertEquals(9, product.size());

    }

    @Test
    public void testStoreTypes() {
        String exprString = "x == 1 || x > 5";
        Store store = storeFromExpr(exprString, "int x");
        List<Expression> product = store.getProductConditions();
        printProduct(exprString, store, product);
        assertEquals(4, product.size());
    }

    /**
     * Print the result of collecting the product of spaces from a {@code Store}
     * 
     * @param exprString
     * @param store
     * @param product
     */
    void printProduct(String exprString, Store store, List<Expression> product) {
        System.out.println("\n\n---- TEST CASE SUMMARY ----");
        System.out.println("[[ expr: " + exprString + " ]]");
        System.out.println(store.pretty());
        System.out.println("Product:");
        for (Expression e : product) {
            System.out.println(" - " + e);
        }
    }

    /**
     * Create a {@code Store} from a given expression and arguments/types.
     * For instance, calling {@code storeFromExpr("x == 1 || x > 5", "int x")}
     * will return a Store for the expression {@code x == 1 || x > 5} where x is
     * an integer.
     * 
     * @param expr the expression to parse
     * @param args the types of each variable in the expression in the form "TYPE
     *             VARIABLE". For instance: "int x", "int y", "char c"
     * @return a {@code Store} for the given expression
     */
    protected Store storeFromExpr(String expr, String... args) {
        String prog = TestUtils.makeClass(expr, args);
        CompilationUnit cu = StaticJavaParser.parse(prog);
        return new Store(rv.collectPredicates(cu));
    }

}
