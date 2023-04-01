package fr.ramatellier.greed.server.compute;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class SocketCandidateReminder {
    private final HashMap<ComputationIdentifier, ArrayList<SocketUcIdentifier>> idToSocketUc= new HashMap<>();
    private final ReentrantLock lock = new ReentrantLock();

    public void storeSocketFor(ComputationIdentifier id, SocketUcIdentifier socketUcIdentifier){
        lock.lock();
        try {
            idToSocketUc.putIfAbsent(id, new ArrayList<>());
            idToSocketUc.get(id).add(socketUcIdentifier);
        } finally {
            lock.unlock();
        }
    }
    public int size(){
        lock.lock();
        try {
            return idToSocketUc.size();
        } finally {
            lock.unlock();
        }
    }
    public int size(ComputationIdentifier id){
        lock.lock();
        try {
            return idToSocketUc.get(id).size();
        } finally {
            lock.unlock();
        }
    }
    public void print(ComputationIdentifier id){
        lock.lock();
        try {
            if(idToSocketUc.get(id).isEmpty()){
                System.out.println("No socket available for id " + id);
                return;
            }
            idToSocketUc.get(id).forEach(System.out::println);
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
