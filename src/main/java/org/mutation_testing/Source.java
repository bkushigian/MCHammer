package org.mutation_testing;

/**
 * A source file and accompanying data
 */
public class Source {
    String filename;
    String contents;
    String[] lines;

    public Source(String filename, String contents) {
        this.filename = filename;
        this.contents = contents;
        this.lines = contents.split("\n");
    }

    public String getFilename() {
        return filename;
    }

    public String getContents() {
        return contents;
    }

    public String[] getLines() {
        return lines;
    }

}
