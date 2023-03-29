package fr.ramatellier.greed.server.compute;


import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Class intends to handle all the computations process and logic.
 */
public final class ComputeWorkHandler {

    private final AtomicLong counterId = new AtomicLong(0L);
    private final AtomicInteger currentNumberComputation = new AtomicInteger(0);
    private final String hostSourceName;
    private final HashMap<ComputationEntity, Boolean> computationsState = new HashMap<>();
    private final HashMap<ComputationEntity, Long> computationResult = new HashMap<>();

    public ComputeWorkHandler(String hostSourceName) {
        this.hostSourceName = Objects.requireNonNull(hostSourceName);
    }

    public void processComputation(ComputationEntity computation) {
        Objects.requireNonNull(computation);
        synchronized (computationsState) {
            if (computationsState.containsKey(computation)) {
                throw new IllegalArgumentException("Computation already exists");
            }
            computationsState.put(computation, false);
        }
    }
    public ComputationIdentifier nextId(){
        var id = counterId.getAndUpdate(x -> x + 1);
        return new ComputationIdentifier(id, hostSourceName);
    }
    public void increaseCurrentNumberComputation(){
        currentNumberComputation.getAndIncrement();
    }
    public void decreaseCurrentNumberComputation(){
        currentNumberComputation.getAndDecrement();
    }

    public int getCurrentNumberComputation() {
        return currentNumberComputation.get();
    }

    //## computation result
    public void addResult(ComputationEntity computation){
        synchronized (computationResult){
            computationResult.putIfAbsent(computation, 0L);
        }
    }
    public void removeResult(ComputationEntity entity){
        synchronized (computationResult){
            computationResult.remove(entity);
        }
    }
    public void incrementResult(ComputationEntity entity){
        synchronized (computationResult){
            computationResult.merge(entity, 0L, (k, v) -> k + 1);
        }
    }
    public boolean isComputationDone(ComputationEntity entity){
        synchronized (computationResult){
            return computationResult.get(entity) == 1 + entity.range().delta();
        }
    }
}
