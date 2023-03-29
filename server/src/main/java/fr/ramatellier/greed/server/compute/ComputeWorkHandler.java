package fr.ramatellier.greed.server.compute;


import fr.ramatellier.greed.server.Server;

import java.net.InetSocketAddress;
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
    private final HashMap<ComputationIdentifier, Boolean> computationsState = new HashMap<>();
    private final HashMap<ComputationEntity, Long> computationResult = new HashMap<>();
    private InetSocketAddress hostAddress;
    public ComputeWorkHandler(InetSocketAddress hostAddress) {
        this.hostAddress = Objects.requireNonNull(hostAddress);
        this.hostSourceName = hostAddress.getHostName();
    }

    public void processComputation(ComputationIdentifier computation) {
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
        return new ComputationIdentifier(id, hostAddress);
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

    /**
     * Check if the server is ready to distribute the computation.
     */
    public boolean isReadyToDistribute(){
        synchronized (computationsState) {
            return computationsState
                    .values()
                    .stream()
                    .allMatch(x -> x);
        }
    }

    /**
     * Initialize a non-ready computation.
     * @param computation the computation to initialize.
     */
    public void createComputation(ComputationIdentifier computation){
        synchronized (computationsState) {
            computationsState.put(computation, false);
        }
    }

    public void switchState(ComputationIdentifier computation, boolean state){
        synchronized (computationsState) {
            if(!computationsState.containsKey(computation)){
                throw new IllegalArgumentException("Computation target does not exists");
            }
            computationsState.merge(computation, false, (k, v) -> state);
        }
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

    public boolean hasEnoughCapacity(long max) {
        return currentNumberComputation.get() + max < Server.MAXIMUM_COMPUTATION;
    }
    public long delta(ComputationEntity entity){
        synchronized (computationResult){
            return entity.range().delta() - currentNumberComputation.get();
        }
    }
}
