package fr.ramatellier.greed.server.compute;

import java.util.ArrayList;
import java.util.HashMap;
public final class ComputationRoomHandler {
    private final Object lock = new Object();
    private final HashMap<Long, CounterIntend> waitingRoom = new HashMap<>();
    private final ArrayList<ComputationEntity> computations = new ArrayList<>();
    public void prepare(ComputationEntity entity, long intendValue) {
        synchronized (lock) {
            waitingRoom.put(entity.getId(), new CounterIntend(intendValue));
            computations.add(entity);
        }
    }

    public void increment(long id) {
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
}
