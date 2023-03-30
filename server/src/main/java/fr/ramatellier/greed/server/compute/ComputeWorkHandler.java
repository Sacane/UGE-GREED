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
    private final HashMap<ComputationEntity, Long> computationResult = new HashMap<>();
    private final HashMap<Long, Integer> computeWorker = new HashMap<>();
    private final InetSocketAddress hostAddress;
    public ComputeWorkHandler(InetSocketAddress hostAddress) {
        this.hostAddress = Objects.requireNonNull(hostAddress);
    }

    public Long nextId(){
        return counterId.getAndIncrement();
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

    public boolean hasEnoughCapacity(long max) {
        return currentNumberComputation.get() + max < Server.MAXIMUM_COMPUTATION;
    }
    public long delta(ComputationEntity entity){
        synchronized (computationResult){
            return entity.range().delta() - currentNumberComputation.get();
        }
    }

    public void initializeComputation(Long info, int numberWorker) {
        synchronized (computeWorker){
            computeWorker.put(info, numberWorker);
        }
    }
    public void incrementComputation(Long info){
        synchronized (computeWorker){
            computeWorker.merge(info, 0, (old_value, new_value) -> old_value + 1);
        }
    }
    public boolean hasAllRespondFor(Long info, long intended){
        synchronized (computeWorker){
            return computeWorker.get(info) == intended;
        }
    }
    public void removeComputation(Long info) {
        synchronized (computeWorker){
            computeWorker.remove(info);
        }
    }

}
