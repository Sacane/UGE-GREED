package fr.ramatellier.greed.server.compute;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

public final class ComputationRoomHandler {
    private final Object lock = new Object();
    private final HashMap<ComputationIdentifier, CounterIntend> waitingRoom = new HashMap<>();
    private final ArrayList<ComputationEntity> computations = new ArrayList<>();
    public void prepare(ComputationEntity entity, long intendValue) {
        synchronized (lock) {
            waitingRoom.put(entity.id(), new CounterIntend(intendValue));
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
            waitingRoom.merge(id, new CounterIntend(1), (old, newOne) -> {
                old.increment();
                return old;
            });
        }
    }
    public boolean isReady(ComputationIdentifier id) {
        synchronized (lock) {
            return waitingRoom.get(id).isReady();
        }
    }

    public Optional<ComputationEntity> findById(ComputationIdentifier id){
        synchronized (lock) {
            return computations.stream().filter(computation -> computation.id().equals(id)).findFirst();
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

    private final static class CounterIntend{
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
