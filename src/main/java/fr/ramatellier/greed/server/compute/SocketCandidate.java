package fr.ramatellier.greed.server.compute;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Store candidates and their UC for each computation
 */
public class SocketCandidate {
    private final HashMap<ComputationIdentifier, ArrayList<SocketUcIdentifier>> idToSocketUc= new HashMap<>();
    private final ReentrantLock lock = new ReentrantLock();

    public void store(ComputationIdentifier id, SocketUcIdentifier socketUcIdentifier){
        Objects.requireNonNull(id);
        Objects.requireNonNull(socketUcIdentifier);
        lock.lock();
        try {
//            idToSocketUc.putIfAbsent(id, new ArrayList<>());
//            idToSocketUc.get(id).add(socketUcIdentifier);
            idToSocketUc.computeIfAbsent(id, k -> new ArrayList<>()).add(socketUcIdentifier);
        } finally {
            lock.unlock();
        }
    }
    public List<SocketUcIdentifier> availableSockets(ComputationIdentifier id){
        lock.lock();
        try {
            return idToSocketUc.get(id)
                    .stream()
                    .filter(socketUcIdentifier -> socketUcIdentifier.uc() > 0)
                    .toList();
        }finally {
            lock.unlock();
        }
    }
}
