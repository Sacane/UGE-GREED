package fr.ramatellier.greed.server.compute;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

/**
 * This is a thread-safe class that has two main purpose:
 * - keep track of all computation that are currently running
 * - prepare computation that are not yet ready to be computed
 * //TODO Later on, we shall separate the two purpose into two different class
 */
public final class ComputationRoomHandler {
    private final Object lock = new Object();
    private final HashMap<ComputationIdentifier, CounterIntend> prepareWaitingRoom = new HashMap<>();
    //TODO move this list into server
    private final ArrayList<ComputationEntity> computations = new ArrayList<>();

    public boolean isComputing() {
        return computations.stream().mapToLong(ComputationEntity::remains).sum() > 0;
    }

    public void incrementComputation(ComputationIdentifier id) {
        var target = computations.stream().filter(computation -> computation.id().equals(id)).findFirst().orElseThrow();
        target.incrementUc();
    }

    /**
     * Prepare a computation that is not yet ready to be computed.
     * @param entity the computation to prepare
     * @param intendValue the number of UC that will be used to compute this computation
     */
    public void prepare(ComputationEntity entity, long intendValue) {
        synchronized (lock) {
            prepareWaitingRoom.put(entity.id(), new CounterIntend(intendValue));
            computations.add(entity);
        }
    }

    public void add(ComputationEntity entity) {
        synchronized (lock) {
            computations.add(entity);
        }
    }

    public void updateRange(ComputationIdentifier id, long start, long end) {
        computations.stream()
                .filter(computation -> computation.id().equals(id))
                .forEach(computation -> computation.setRange(start, end));
    }


    /**
     * Increment the number of UC that are ready to be used to compute the target computation.
     * @param id the computation to increment
     */
    public void increment(ComputationIdentifier id) {
        synchronized (lock) {
            prepareWaitingRoom.merge(id, new CounterIntend(1), (old, newOne) -> {
                old.increment();
                return old;
            });
        }
    }

    /**
     * Check if the computation is ready to be computed.
     * @param id the computation to check
     * @return true if the target computation is ready to be computed, false otherwise
     */
    public boolean isReady(ComputationIdentifier id) {
        synchronized (lock) {
            return prepareWaitingRoom.get(id).isReady();
        }
    }

    public Optional<ComputationEntity> findById(ComputationIdentifier id){
        synchronized (lock) {
            return computations.stream().filter(computation -> computation.id().equals(id)).findFirst();
        }
    }

    public void incrementUc(ComputationIdentifier id){
        synchronized (lock) {
            computations
                    .stream()
                    .filter(computation -> computation.id().equals(id))
                    .findFirst()
                    .ifPresent(ComputationEntity::incrementUc);
        }
    }

    public boolean isComputationDone(ComputationIdentifier id){
        synchronized (lock) {
            return computations
                    .stream()
                    .filter(computation -> computation.id().equals(id))
                    .findFirst()
                    .map(ComputationEntity::isReady)
                    .orElse(false);
        }
    }

    @Override
    public String toString() {
        synchronized (lock) {
            var builder = new StringBuilder();
            for (var computation : computations) {
                builder.append(computation.info());
            }
            return builder.toString();
        }
    }

    private final static class CounterIntend {
        private final long intendValue;
        private long countedValue;

        public CounterIntend(long intendValue) {
            this.intendValue = intendValue;
        }
        private void increment() {
            if(countedValue == intendValue){
                return;
            }
            countedValue++;
        }
        private boolean isReady(){
            return countedValue == intendValue;
        }
    }
}
