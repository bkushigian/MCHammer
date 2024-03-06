package org.mutation_testing;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Before;
import org.junit.Test;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

public class MCsCollectorTest {
    @Before
    public void setUp() {
        CombinedTypeSolver typeSolver = new CombinedTypeSolver();
        typeSolver.add(new ReflectionTypeSolver());
        StaticJavaParser.getParserConfiguration().setSymbolResolver(new JavaSymbolSolver(typeSolver));
        MCs.setOptimize(true);
    }

    Map<MCs, Node> runOnProgram(String p) {
        System.out.println("-----------------------------");
        System.out.println("Collecting MCs from\n" + p);
        CompilationUnit cu = StaticJavaParser.parse(p);
        MethodDeclaration md = cu.findFirst(MethodDeclaration.class).get();
        System.out.println("Method: " + md.toString());
        MCsCollector collector = new MCsCollector();
        collector.collectMutationConditions(md);
        for (Entry<MCs, Node> entry : collector.endBlock.entrySet()) {
            System.out.println("     ------------");
            MCs mcs = entry.getKey();
            System.out.println("MCs:       " + mcs);
            MCs optimized = MCs.optimize(mcs);
            System.out.println("Optimized: " + optimized);
            Node node = entry.getValue();
            System.out.println("Node: " + node);

            List<Expression> conditions = optimized.toConditions();
            System.out.println("Conditions");
            System.out.println("==========");
            for (int i = 0; i < conditions.size(); i++) {
                System.out.println(" (" + (i + 1) + ") " + conditions.get(i));
            }

        }

        return collector.endBlock;
    }

    @Test
    public void testCollectMutationConditions01() {
        runOnProgram("class A { boolean t() { return true;}}");
    }

    @Test
    public void testCollectMutationConditions02() {
        runOnProgram("class A { boolean f() { return false;}}");
    }

    @Test
    public void testCollectMutationConditions03() {
        runOnProgram("class A { boolean ite(boolean c, int a, int b) { if (c) return a; return b;}}");
    }

    @Test
    public void testCollectMutationConditions04() {
        runOnProgram("class A { boolean max(int a, int b) { if (a > b) return a; return b;}}");
    }

    @Test
    public void testCollectMutationConditions05() {
        runOnProgram(
                "class A { boolean max(int a, int b) { int max; if (a > b) {max = a;} else {max = b;} return max;}}");
    }

    @Test
    public void testCollectMutationConditions06() {
        runOnProgram(
                "class A { boolean max(int a, int b) { int max = b; if (a > b) {max = a;} return max;}}");
    }

    @Test
    public void testCollectMutationConditions07() {
        String p = "class A {" +
                "  boolean max(int a, int b, int c) {" +
                "    if (a > b) {" +
                "      if (a > c) return a;" +
                "      return c;" +
                "    } else if (b > c) {" +
                "      return b;" +
                "    }" +
                "    return c;" +
                "  }" +
                "}";
        runOnProgram(p);
    }

}
