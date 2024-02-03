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
    String outdir = "msav_out";
    String mutantsLog = "mutants.msav.log";
    Mutator mutator = new Mutator();

    public App(List<String> filenames, String outdir, String mutantsLog) {
        this.filenames = filenames;
        this.outdir = outdir;
        this.mutantsLog = mutantsLog;
    }

    public static void main(String[] args) {
        String outdir = "msav_out";
        String mutantsLog = "mutants.msav.log";
        List<String> filenames = new ArrayList<>();
        for (int i = 0; i < args.length; ++i) {
            if ("--outdir".equals(args[i])) {
                i += 1;
                if (i >= args.length) {
                    System.err.println("Missing argument for --outdir");
                    System.exit(1);
                }
                outdir = args[i];
                continue;
            } else if ("--log".equals(args[i])) {
                i += 1;
                if (i >= args.length) {
                    System.err.println("Missing argument for --log");
                    System.exit(1);
                }
                mutantsLog = args[i];
                continue;
            }
            filenames.add(args[i]);
        }

        App app = new App(filenames, outdir, mutantsLog);
        app.run();
    }

    void run() {
        final List<Mutant> mutants = new ArrayList<>();
        if (filenames.isEmpty()) {
            System.out.println("No files to mutate");
            return;
        }
        for (String filename : filenames) {
            try {
                mutants.addAll(mutator.mutateFile(filename));
            } catch (IOException e) {
                System.err.println("Error mutating file " + filename);
                e.printStackTrace();
            }
        }

        // Check if outdir exists, and if not, create it
        Path outdirPath = Paths.get(outdir);
        Path mutantsDir = outdirPath.resolve("mutants");
        if (!Files.exists(outdirPath)) {
            try {
                Files.createDirectories(outdirPath);
                Files.createDirectories(mutantsDir);
            } catch (IOException e) {
                System.err.println("Error creating directory " + outdir);
                e.printStackTrace();
                return;
            }
        }

        System.out.println("Writing mutant log to " + mutantsLog);
        Mutant.writeMutantsLog(mutantsLog, mutants);

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
        System.out.println("Generated " + mutants.size() + " mutants in " + mutantsDir);
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
