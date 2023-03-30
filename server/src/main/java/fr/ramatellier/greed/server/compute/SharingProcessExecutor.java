package fr.ramatellier.greed.server.compute;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class SharingProcessExecutor {
    private final HashMap<SocketUcIdentifier, Long> availableSocketMap = new HashMap<>();
    private final long nbWorkingComputation;
    public SharingProcessExecutor(List<SocketUcIdentifier> availableSocketList, long nbWorkingComputation) {
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
    public List<SocketRange> shareAndGet(){
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
        var start = 0L;
        for(var socketUcIdentifier : availableSocketMap.keySet()){
            var end = start + availableSocketMap.get(socketUcIdentifier);
            var socketRange = new SocketRange(socketUcIdentifier.address(), new Range(start, end));
            start = end;
            socketRangeList.add(socketRange);
        }
        return socketRangeList;
    }
}
