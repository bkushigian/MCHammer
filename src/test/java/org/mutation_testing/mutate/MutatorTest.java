package org.mutation_testing.mutate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;
import org.mutation_testing.TestUtils;

public class MutatorTest {

    @Test
    public void testMutate01() {
        String prog = TestUtils.makeClass("x >= 32 && x < 127", "int x");
        assertExpectedMutationConditions(prog, "x <= 31", "x == 32", "x >= 33 && x <= 126", "x == 127", "x >= 128");
    }

    @Test
    public void testMutate02() {
        String prog = TestUtils.makeClass("x == 32 || x > 127", "int x");
        assertExpectedMutationConditions(prog, "x == 32", "x <= 126 && x != 32", "x == 127", "x >= 128");
    }

    @Test
    public void testMutate03() {
        String prog = TestUtils.makeClass("x == 32 && y == 32", "int x", "int y");
        assertExpectedMutationConditions(prog, "x != 32 && y != 32", "x == 32 && y != 32", "x != 32 && y == 32",
                "x == 32 && y == 32");
    }

    @Test
    public void testMutate04() {
        String prog = TestUtils.makeClass("s.equals(\"foo\")", "String s");
        System.out.println("Program:\n" + prog);
        assertExpectedMutationConditions(prog, "s.equals(\"foo\")", "!s.equals(\"foo\")");
    }

    @Test
    public void testMutate05() {
        String prog = TestUtils.makeClass("s.equals(\"foo\") || t.startsWith(\"bar\")", "String s", "String t");
        assertExpectedMutationConditions(prog,
                "s.equals(\"foo\") && t.startsWith(\"bar\")",
                "s.equals(\"foo\") && !t.startsWith(\"bar\")",
                "!s.equals(\"foo\") && t.startsWith(\"bar\")",
                "!s.equals(\"foo\") && !t.startsWith(\"bar\")");
    }

    /**
     * Assert that the returned mutation conditions are as expected.
     * 
     * @param mutants
     * @param expected
     */
    private void assertExpectedMutationConditions(String prog, String... expectedConditions) {
        System.out.println("Mutating: " + prog);
        List<Mutant> mutants = new Mutator().mutate("TestClass.java", prog);
        System.out.println("Mutants: " + mutants.size() + " " + mutants);

        // Compare the {@code toString} of the mutation conditions: the literal
        // types from the mutants don't always align with what the
        // StaticJavaParser would produce, and this makes direct node-to-node
        // comparisons difficult.
        Set<String> actual = mutants.stream().map(m -> m.getMutationCondition().toString()).collect(Collectors.toSet());
        List<String> expected = Arrays.asList(expectedConditions);

        assertEquals(expected.size(), actual.size());

        for (String e : expected) {
            assertTrue("Expected condition not found: " + e, actual.contains(e));
        }
    }

}
