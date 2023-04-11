package fr.ramatellier.greed.server.compute;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

/**
 * This class has two main purpose:
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

    //TODO increment by Id not by info
    public void incrementComputation(ComputeInfo info) {
        computations.stream().filter(computation -> computation.info().equals(info)).forEach(ComputationEntity::incrementUc);
    }

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

    public void increment(ComputationIdentifier id) {
        synchronized (lock) {
            prepareWaitingRoom.merge(id, new CounterIntend(1), (old, newOne) -> {
                old.increment();
                return old;
            });
        }
    }

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
