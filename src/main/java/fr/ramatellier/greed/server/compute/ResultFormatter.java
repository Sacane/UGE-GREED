package fr.ramatellier.greed.server.compute;

import fr.ramatellier.greed.server.util.ResponseToFileBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This thread-safe class is aim to store and format the result of a computation.
 * By its ID the result will be stored in a file.
 */
public final class ResultFormatter {
    private final ReentrantLock lock = new ReentrantLock();
    private final HashMap<ComputationIdentifier, ResponseToFileBuilder> computeToBuilder = new HashMap<>();

    /**
     * Append a result to the file builder
     * @param id the computation identifier
     * @param result the result to append
     */
    public void append(ComputationIdentifier id, String result) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(result);
        lock.lock();
        try {
            var fileBuilder = (computeToBuilder.containsKey(id)) ?
                    computeToBuilder.get(id) :
                    new ResponseToFileBuilder(id.outputTitle());
            fileBuilder.append(result);
            computeToBuilder.put(id, fileBuilder);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Build the file and remove the builder from the map
     * @param id the computation identifier
     * @throws IOException if an error occurs while writing the file
     */
    public void build(ComputationIdentifier id) throws IOException {
        Objects.requireNonNull(id);
        lock.lock();
        try {
            if(computeToBuilder.containsKey(id)){
                computeToBuilder.get(id).build();
                computeToBuilder.remove(id);
            }
        } finally {
            lock.unlock();
        }
    }
}
