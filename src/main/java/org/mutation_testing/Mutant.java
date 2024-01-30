package org.mutation_testing;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.StringJoiner;

import com.github.javaparser.Position;
import com.github.javaparser.ast.Node;

public class Mutant {
    int mid;
    Node origNode;
    Node replNode;
    Source source;

    public Mutant(int mid, Source source, Node originalNode, Node replNode) {
        this.mid = mid;
        this.source = source;
        this.origNode = originalNode;
        this.replNode = replNode;
    }

    public String asFileString() {
        String[] lines = source.getLines();

        Position begin = origNode.getBegin().get();
        Position end = origNode.getEnd().get();
        int beginLine = begin.line - 1;
        int beginColumn = begin.column - 1;
        int endLine = end.line - 1;
        int endColumn = end.column - 1;

        StringJoiner sj = new StringJoiner("\n");

        // First, get everything before the mutated node
        for (int i = 0; i < beginLine; i++) {
            sj.add(lines[i]);
        }

        // Now, build the mutated node's replacement string
        StringBuilder replBuilder = new StringBuilder();
        replBuilder.append(lines[beginLine].substring(0, beginColumn));
        replBuilder.append(replNode.toString());
        replBuilder.append(lines[endLine].substring(endColumn));
        sj.add(replBuilder.toString());

        // Finally, get everything after the mutated node
        for (int i = endLine + 1; i < lines.length; i++) {
            sj.add(lines[i]);
        }

        return sj.toString();
    }

    public String toString() {
        StringJoiner sj = new StringJoiner(",");
        sj.add("" + mid)
                .add(source.getFilename())
                .add(origNode.getBegin().get().toString())
                .add(origNode.getEnd().get().toString())
                .add(origNode.toString())
                .add(replNode.toString());

        return sj.toString();
    }

    public String asMajorLogItem() {
        StringJoiner sj = new StringJoiner(":");
        String origString = origNode.toString();
        String replString = replNode.toString();
        String mutantLogString = origString + " |==> " + replString;
        sj.add("" + mid)
                .add("MSAV")
                .add("")
                .add("")
                .add(this.source.getFilename())
                .add(origNode.getBegin().get().toString())
                .add(mutantLogString);

        return sj.toString();
    }

    public static void writeMutantsLog(String logFile, List<Mutant> mutants) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(logFile))) {
            for (Mutant mutant : mutants) {
                pw.println(mutant.asMajorLogItem());
            }
        } catch (IOException e) {
            System.err.println("Error writing mutants log file " + logFile);
            e.printStackTrace();
        }
    }
}
