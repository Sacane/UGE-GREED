package fr.ramatellier.greed.server.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;

public final class ResponseToFileBuilder {
    private final StringBuilder builder = new StringBuilder();
    private final String fileName;

    public ResponseToFileBuilder(String fileName) {
        this.fileName = Objects.requireNonNull(fileName);
    }

    public ResponseToFileBuilder append(String str) {
        builder.append(str);
        return this;
    }

    public boolean build() throws IOException {
        System.out.println("Build file " + fileName);
        var file = new File(fileName);
        try(var writer = new FileWriter(file)){
            writer.write(builder.toString());
        }
        System.out.println(file.getAbsolutePath());
        return file.createNewFile();
    }
}
