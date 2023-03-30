package fr.ramatellier.greed.server.compute;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class SocketCandidateReminder {
    private final HashMap<ComputationIdentifier, HashSet<SocketUcIdentifier>> idToSocketUc= new HashMap<>();
    private final ReentrantLock lock = new ReentrantLock();

    public void storeSocketFor(ComputationIdentifier id, SocketUcIdentifier socketUcIdentifier){
        try {
            lock.lock();
            idToSocketUc.merge(id, new HashSet<>(), (old, newOne) -> {
                old.add(socketUcIdentifier);
                return old;
            });
        } finally {
            lock.unlock();
        }
    }
    public boolean isCapacityEnough(ComputationIdentifier id, long capacityComputationValue){
        lock.lock();
        try{
            return idToSocketUc.get(id)
                    .stream()
                    .mapToLong(SocketUcIdentifier::uc)
                    .sum() >= capacityComputationValue;
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
