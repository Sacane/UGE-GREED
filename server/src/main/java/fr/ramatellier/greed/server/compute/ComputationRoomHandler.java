package fr.ramatellier.greed.server.compute;

import java.util.ArrayList;
import java.util.HashMap;
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

    public void increment(ComputationIdentifier id) {
        synchronized (waitingRoom) {
            waitingRoom.merge(id, new CounterIntend(1), (old, newOne) -> {
                old.increment();
                return old;
            });
        }
    }
    public boolean isReady(long id) {
        synchronized (waitingRoom) {
            return waitingRoom.get(id).isReady();
        }
    }

    public long getIntendedValue(ComputationIdentifier id){
        synchronized (waitingRoom) {
            return waitingRoom.get(id).intendValue;
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
                throw new IllegalStateException("CounterIntend is already full");
            }
            countedValue++;
        }
        private boolean isReady(){
            return countedValue == intendValue;
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
}
