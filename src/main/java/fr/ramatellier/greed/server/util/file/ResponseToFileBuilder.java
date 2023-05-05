package fr.ramatellier.greed.server.util.file;

import fr.ramatellier.greed.server.Application;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * This class is aim to build a file response, using the builder pattern throughout a StringBuilder.
 */
public final class ResponseToFileBuilder {
    private static final Logger logger = Logger.getLogger(Application.class.getName());
    private final StringBuilder builder = new StringBuilder();
    private final String fileName;

    public ResponseToFileBuilder(String fileName) {
        this.fileName = Objects.requireNonNull(fileName);
    }

    /**
     * Append a string to the StringBuilder.
     * @param str the string to append
     * @return the updated builder
     */
    public ResponseToFileBuilder append(String str) {
        builder.append(str);
        return this;
    }

    /**
     * Build a file from the StringBuilder content.
     * @throws IOException if an error occurs while writing the file
     */
    public void build() throws IOException {
        if(builder.length() == 0){
            logger.warning("Builder is empty, nothing to write");
            return;
        }
        var file = new File(fileName);
        try(var writer = new FileWriter(file)){
            writer.write(builder.toString());
        }finally {
            logger.info("Result has been created into -> " + file.getAbsolutePath());
        }
    }
}
