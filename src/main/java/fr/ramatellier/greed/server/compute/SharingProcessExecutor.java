package fr.ramatellier.greed.server.compute;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This class will share the computation between the different socket.
 * It will return the range of computation for each socket that has been assigned
 */
public class SharingProcessExecutor {
    private final HashMap<SocketUcIdentifier, Long> availableSocketMap = new HashMap<>();
    private final long nbWorkingComputation;
    public SharingProcessExecutor(List<SocketUcIdentifier> availableSocketList, long nbWorkingComputation) {
        if(availableSocketList.isEmpty())
            throw new IllegalArgumentException("availableSocketList can't be empty");
        this.nbWorkingComputation = nbWorkingComputation;
        for(var availableSocket : availableSocketList){
            if(availableSocket.uc() > 0) {
                availableSocketMap.put(availableSocket, 0L);
            }
        }
    }

    private void incrementUc(SocketUcIdentifier socketUcIdentifier){
        availableSocketMap.merge(socketUcIdentifier, 0L, (old, newOne) -> old + 1);
    }

    /**
     * This method will share the computation and return the range of computation for each socket that has been assigned.
     * @param start the start of the range
     * @return the list of socket range computed
     */
    public List<SocketRange> shareAndGet(long start){
        var socketRangeList = new ArrayList<SocketRange>();
        var computingLeft = nbWorkingComputation;
        while(computingLeft > 0){
            for (var socketUcIdentifier : availableSocketMap.keySet()) {
                if(computingLeft > 0 && socketUcIdentifier.uc() > availableSocketMap.get(socketUcIdentifier)){
                    incrementUc(socketUcIdentifier);
                    computingLeft--;
                }
            }
        }
        var startRange = start;
        for(var socketUcIdentifier : availableSocketMap.keySet()){
            var end = startRange + availableSocketMap.get(socketUcIdentifier);
            var socketRange = new SocketRange(socketUcIdentifier.address(), new Range(startRange, end));
            startRange = end;
            socketRangeList.add(socketRange);
        }
        return socketRangeList;
    }
}
