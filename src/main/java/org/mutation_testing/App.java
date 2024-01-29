package org.mutation_testing;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Hello world!
 *
 */
public class App {
    List<String> filenames = new ArrayList<>();
    String outdir = "msav_out";
    Mutator mutator = new Mutator();

    public App(List<String> filenames, String outdir) {
        this.filenames = filenames;
        this.outdir = outdir;
    }

    public static void main(String[] args) {
        String outdir = "msav_out";
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
            }
            filenames.add(args[i]);
        }

        App app = new App(filenames, outdir);
        app.run();
    }

    void run() {
        List<Mutant> mutants = new ArrayList<>();
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
        for (Mutant mutant : mutants) {
            Path d = mutantsDir.resolve(mutant.mid + "");
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
            Path mutantFilename = Paths.get(mutant.source.getFilename()).getFileName();
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
            for (File c : f.listFiles()){
                delete(c);
            }
        }
        if (!f.delete()) {
            System.out.println("Failed to delete file: " + f);
        }
    }
}
