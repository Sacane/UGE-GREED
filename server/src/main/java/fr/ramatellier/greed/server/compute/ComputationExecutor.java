package fr.ramatellier.greed.server.compute;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ComputationExecutor {
    private final HashMap<Long, List<InetSocketAddress>> computationWorkers = new HashMap<>();
    private final HashMap<ComputationIdentifier, Long> computationCapacity = new HashMap<>();
    public void mergeWorkerFor(Long id, InetSocketAddress address){
        computationWorkers.merge(id, new ArrayList<>(), (old, newValue) -> {
            old.add(address);
            return old;
        });
    }

    public void addCapacity(ComputationIdentifier identifier, long capacity){
        computationCapacity.put(identifier, capacity);
    }
}
