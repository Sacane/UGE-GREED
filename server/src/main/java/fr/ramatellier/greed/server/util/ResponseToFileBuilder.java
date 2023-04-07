package fr.ramatellier.greed.server.util;

import java.io.File;
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

    public File build() {
        return new File(fileName, builder.toString());
    }
}
