package org.mutation_testing;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.mutation_testing.mutate.Mutant;
import org.mutation_testing.mutate.Mutator;

/**
 * Hello world!
 *
 */
public class App {
    List<String> filenames = new ArrayList<>();
    List<String> sourceRoots = new ArrayList<>();
    String outdir = "msav_out";
    String mutantsLog = "mutants.msav.log";
    Mutator mutator = new Mutator();

    public static void main(String[] args) {
        App app = new App();
        app.parseArgs(args);
        app.run();
    }

    void run() {
        if (filenames.isEmpty()) {
            System.err.println("No files to mutate");
            return;
        }
        final List<Mutant> mutants = mutateFilenames();
        Mutant.writeMutantsLog(mutantsLog, mutants);
        writeMutantsToDisk(mutants);
        System.out.println("Generated " + mutants.size() + " mutants in " + mutantsDir());
    }

    List<Mutant> mutateFilenames() {
        List<Mutant> mutants = new ArrayList<>();
        for (String filename : filenames) {
            try {
                mutants.addAll(mutator.mutateFile(filename));
            } catch (IOException e) {
                System.err.println("Error mutating file " + filename);
                e.printStackTrace();
            }
        }
        return mutants;
    }

    Path mutantsDir() {
        return Paths.get(outdir).resolve("mutants");
    }

    Path mutantsLogPath() {
        return Paths.get(outdir).resolve(mutantsLog);
    }

    private void parseArgs(String... args) {
        int argIndex = 0;
        while (argIndex < args.length) {
            if ("--outdir".equals(args[argIndex])) {
                argIndex += 1;
                if (argIndex >= args.length) {
                    System.err.println("Missing argument for --outdir");
                    System.exit(1);
                }
                outdir = args[argIndex];
            } else if ("--log".equals(args[argIndex])) {
                argIndex += 1;
                if (argIndex >= args.length) {
                    System.err.println("Missing argument for --log");
                    System.exit(1);
                }
                mutantsLog = args[argIndex];
            } else if ("--sourceroot".equals(args[argIndex])) {
                argIndex += 1;
                if (argIndex >= args.length) {
                    System.err.println("Missing argument for --sourceroot");
                    System.exit(1);
                }
                sourceRoots.add(args[argIndex]);
            } else {
                filenames.add(args[argIndex]);
            }
            argIndex += 1;
        }
    }

    Path makeMutantsDir() {
        Path outdirPath = Paths.get(outdir);
        Path mutantsDir = outdirPath.resolve("mutants");
        if (!Files.exists(outdirPath)) {
            try {
                Files.createDirectories(outdirPath);
                Files.createDirectories(mutantsDir);
            } catch (IOException e) {
                System.err.println("Error creating directory " + outdir);
                e.printStackTrace();
                return null;
            }
        }
        return mutantsDir;
    }

    void writeMutantsToDisk(List<Mutant> mutants) {

        Path mutantsDir = makeMutantsDir();

        for (Mutant mutant : mutants) {
            Path d = mutantsDir.resolve(mutant.getMid() + "");
            if (Files.exists(d)) {
                delete(d.toFile());
            }
            try {
                Files.createDirectories(d);
            } catch (IOException e) {
                System.err.println("Error creating directory " + d);
                e.printStackTrace();
                return;
            }
            String mutantFileContents = mutant.asFileString();
            Path mutantFilename = Paths.get(mutant.getSource().getFilename()).getFileName();
            // Create a new file for the mutant
            Path mutantFilePath = d.resolve(mutantFilename);
            try {
                Files.write(mutantFilePath, mutantFileContents.getBytes());
            } catch (IOException e) {
                System.err.println("Error writing file " + mutantFilePath);
                e.printStackTrace();
                return;
            }
        }

    }

    private void delete(File f) {
        if (f.isDirectory()) {
            for (File c : f.listFiles()) {
                delete(c);
            }
        }
        if (!f.delete()) {
            System.out.println("Failed to delete file: " + f);
        }
    }
}
