package org.mutation_testing.smt;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.mutation_testing.JavaFileBuilder;
import org.mutation_testing.MCs;
import org.mutation_testing.MCsCollector;
import org.mutation_testing.TestUtils;

import com.github.javaparser.Position;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.printer.configuration.PrettyPrinterConfiguration;
import com.github.javaparser.printer.configuration.PrinterConfiguration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

public class SMTConstraintGeneratorTest {

    PrinterConfiguration prettyPrintConf = new PrettyPrinterConfiguration();
    @Before
    public void setUp() {
        CombinedTypeSolver typeSolver = new CombinedTypeSolver();
        typeSolver.add(new ReflectionTypeSolver());
        StaticJavaParser.getParserConfiguration().setSymbolResolver(new JavaSymbolSolver(typeSolver));
        MCs.setOptimize(true);

    }

    MCsCollector collectMCs(MethodDeclaration method) {
        MCsCollector collector = new MCsCollector();
        System.out.println("=== Collecting Mutation Conditions for Method: " + method.getNameAsString() + " ===");
        System.out.println("```");
        System.out.println(method);
        System.out.println("```");

        collector.collectMutationConditions(method);
        System.out.println("Raw Mutation Conditions: " + collector.getEndBlock().keySet());
        return collector;
    }

    /** 
     * Helper method that collects mutation conditions, computes the satisfiable
     * MCs, asserts the number of satisfiable MCs, and returns a map from
     * collections of MCs to nodes
     */
    public Map<NodeWithPos, List<Expression>> getNodesToMCs(MCsCollector collector) {
        List<Expression> sat = new ArrayList<>();

        Map<NodeWithPos, List<Expression>> nodeToMCs = new HashMap<>();

        for (Map.Entry<MCs, Node> entry : collector.getEndBlock().entrySet()) {
            MCs mcs = entry.getKey();
            Node node = entry.getValue();

            System.out.println("----------- MUTATION CONDITIONS " + mcs + " -----------" );
            MCs optimized = MCs.optimize(mcs);
            System.out.println("Optimized: " + optimized);
            List<Expression> satMCs = optimized.toSATConditions();
            if (satMCs.size() > 0) {
                System.out.println("Node: " + node);
                System.out.println("Position: " + node.getBegin().get());
                System.out.println("Found " + satMCs.size() + " SAT Mutation Conditions: "); 
                int i = 0;
                for (Expression e : satMCs) {
                    i ++;
                    System.out.println("  " + i + ": " + e.toString(prettyPrintConf));
                }
                sat.addAll(satMCs);
                nodeToMCs.put(new NodeWithPos(node), satMCs);
            }
        }
        System.out.println(sat);
        return nodeToMCs;
    }
    @Test
    public void testGenerateConstraintsLT01() {
        String p = TestUtils.makeClassFromBody("int",
         "max",
         "int m = 0; if (a > b) { m = a; } else { m = b; } return m;",
          "int a", "int b");
        TestUtils.printProgramWithLinenumbers(p);
        MethodDeclaration m = TestUtils.getMethod(p);
        MCsCollector collector = collectMCs(m);
        Map<NodeWithPos, List<Expression>> nodesToMCs = getNodesToMCs(collector);
        List<Expression> allMCs = nodesToMCs.values().stream().flatMap(List::stream).collect(Collectors.toList());
        assertEquals(3, allMCs.size());
    }


    @Test
    public void testGenerateConstraintsLT02() {
        String p = TestUtils.makeClassFromBody("int",
         "max",
         "if (a > b) { return a; } else { return b; }",
          "int a", "int b");
        TestUtils.printProgramWithLinenumbers(p);
        MethodDeclaration m = TestUtils.getMethod(p);
        MCsCollector collector = collectMCs(m);
        Map<NodeWithPos, List<Expression>> nodesToMCs = getNodesToMCs(collector);
        List<Expression> allMCs = nodesToMCs.values().stream().flatMap(List::stream).collect(Collectors.toList());
        assertEquals(3, allMCs.size());
    }

    @Test
    public void testGenerateConstraintsLT03() {
        String p = TestUtils.makeClassFromBody("int",
         "max3",
         "if (a > b) { if (a > c) {return a;} else { return c;} } else if (b > c) { return b; } else {return c;}",
          "int a", "int b", "int c");
        TestUtils.printProgramWithLinenumbers(p);
        MethodDeclaration m = TestUtils.getMethod(p);
        MCsCollector collector = collectMCs(m);
        Map<NodeWithPos, List<Expression>> nodesToMCs = getNodesToMCs(collector);
        List<Expression> allMCs = nodesToMCs.values().stream().flatMap(List::stream).collect(Collectors.toList());
        System.out.println("   === All MCs ===");
        int mcNo = 0;
        for (Expression e : allMCs) {
            mcNo += 1;
            System.out.println("  " + mcNo + ": " + e);
        }
        System.out.println("Number of MCs: " + allMCs.size());
    }

    @Test
    public void testGenerateConstraintsEq01() {
        String p = TestUtils.makeClassFromExpr("boolean",
         "isVowel",
         "ch == 'a' || ch == 'e' || ch == 'i' || ch == 'o' || ch == 'u'",
          "char ch");
        MethodDeclaration m = TestUtils.getMethod(p);
        MCsCollector collector = collectMCs(m);
        Map<NodeWithPos, List<Expression>> nodesToMCs = getNodesToMCs(collector);
        List<Expression> allMCs = nodesToMCs.values().stream().flatMap(List::stream).collect(Collectors.toList());
        assertEquals(6, allMCs.size());
    }

    @Test
    public void testGenerateConstraintsMethodCall01() {
        JavaFileBuilder b = new JavaFileBuilder("TestClass");
        b.addImport("java.util.*");
        b.addMethod("boolean", "isEmpty", new String[] {"List l"}, "return l.isEmpty();");
        String p = b.toString();
        System.out.println(p);
        MethodDeclaration m = TestUtils.getMethod(p);
        MCsCollector collector = collectMCs(m);
        Map<NodeWithPos, List<Expression>> nodesToMCs = getNodesToMCs(collector);
        List<Expression> allMCs = nodesToMCs.values().stream().flatMap(List::stream).collect(Collectors.toList());
        assertEquals(2, allMCs.size());
    }

    @Test
    public void testGenerateConstraintsMethodCall02() {
        JavaFileBuilder b = new JavaFileBuilder("TestClass");
        b.addMethod("boolean", "hasVowel", new String[] {"String s"}, "return s.contains(\"a\") || s.contains(\"e\");");
        String p = b.toString();
        System.out.println(p);
        MethodDeclaration m = TestUtils.getMethod(p);
        MCsCollector collector = collectMCs(m);
        Map<NodeWithPos, List<Expression>> nodesToMCs = getNodesToMCs(collector);
        List<Expression> allMCs = nodesToMCs.values().stream().flatMap(List::stream).collect(Collectors.toList());
        // Expect 3 MCs:
        // Yes a
        // No a, yes e
        // No a, no e
        assertEquals(3, allMCs.size());

    }

    @Test
    public void testGenerateConstraintsMethodCall03() {
        JavaFileBuilder b = new JavaFileBuilder("TestClass");
        b.addMethod("boolean", "hasVowel", new String[] {"String s"}, "return s.contains(\"a\") || s.contains(\"e\") || s.contains(\"i\");");
        String p = b.toString();
        System.out.println(p);
        MethodDeclaration m = TestUtils.getMethod(p);
        MCsCollector collector = collectMCs(m);
        Map<NodeWithPos, List<Expression>> nodesToMCs = getNodesToMCs(collector);
        List<Expression> allMCs = nodesToMCs.values().stream().flatMap(List::stream).collect(Collectors.toList());
        // Expect 3 MCs:
        // Yes a
        // No a, yes e
        // No a, no e
        assertEquals(4, allMCs.size());

    }


    public static class NodeWithPos { 
        public final Node node;
        public final Position position;

        NodeWithPos(Node node, Position position) {
            this.node = node;
            this.position = position;
        }

        NodeWithPos(Node node) {
            this.node = node;
            this.position = node.getBegin().get();
        }

        public String toString() {
            return String.format("Node: %s, Position: %s", node, position);
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof NodeWithPos)) {
                return false;
            }
            NodeWithPos n = (NodeWithPos) o;
            return n.node.equals(node) && n.position.equals(position);
        }

        @Override
        public int hashCode() {
            int prime = 31;
            int result = 1;
            result = prime * result + node.hashCode();
            result = prime * result + position.hashCode();
            return result;
        }
    }

}
