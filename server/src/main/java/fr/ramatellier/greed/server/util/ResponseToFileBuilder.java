package fr.ramatellier.greed.server.util;

import fr.ramatellier.greed.server.Server;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;
import java.util.logging.Logger;

public final class ResponseToFileBuilder {
    private static final Logger logger = Logger.getLogger(Server.class.getName());
    private final StringBuilder builder = new StringBuilder();
    private final String fileName;

    public ResponseToFileBuilder(String fileName) {
        this.fileName = Objects.requireNonNull(fileName);
    }

    public ResponseToFileBuilder append(String str) {
        builder.append(str);
        return this;
    }

    public void build() throws IOException {
        var file = new File(fileName);
        try(var writer = new FileWriter(file)){
            writer.write(builder.toString());
        }
        System.out.println(file.getAbsolutePath());
        if(!file.createNewFile()) {
            logger.severe("Failed to create the file response");
        }
    }
}
