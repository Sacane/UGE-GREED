package fr.ramatellier.greed.server.util;

import fr.ramatellier.greed.server.compute.ComputationIdentifier;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

public final class ResultFormatHandler {
    private final ReentrantLock lock = new ReentrantLock();
    private final HashMap<ComputationIdentifier, ResponseToFileBuilder> computeToBuilder = new HashMap<>();

    public void append(ComputationIdentifier id, String result){
        lock.lock();
        try {
            computeToBuilder.merge(id, new ResponseToFileBuilder(id.outputTitle()), (old, newOne) -> old.append(result));
        } finally {
            lock.unlock();
        }
    }
    public boolean build(ComputationIdentifier id) throws IOException {
        lock.lock();
        try {
            if(computeToBuilder.containsKey(id)){
                computeToBuilder.get(id).build();
                computeToBuilder.remove(id);
                return true;
            }
            return false;
        } finally {
            lock.unlock();
        }
    }

}
