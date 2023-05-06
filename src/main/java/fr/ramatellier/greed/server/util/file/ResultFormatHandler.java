package fr.ramatellier.greed.server.util.file;

import fr.ramatellier.greed.server.compute.ComputationIdentifier;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;
//TODO Johan
public final class ResultFormatHandler {
    private final ReentrantLock lock = new ReentrantLock();
    private final HashMap<ComputationIdentifier, ResponseToFileBuilder> computeToBuilder = new HashMap<>();

    public void append(ComputationIdentifier id, String result) {
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
     * @param id
     * @throws IOException
     */
    public void build(ComputationIdentifier id) throws IOException {
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
