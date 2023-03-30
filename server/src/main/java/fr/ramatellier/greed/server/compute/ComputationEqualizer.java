package fr.ramatellier.greed.server.compute;

import java.util.HashMap;
import java.util.HashSet;

public class ComputationEqualizer {
    private final HashMap<Long, HashSet<SocketUcIdentifier>> idToSocketUc= new HashMap<>();

    public void storeSocketFor(long id, SocketUcIdentifier socketUcIdentifier){
        idToSocketUc.merge(id, new HashSet<>(), (old, newOne) -> {
            old.add(socketUcIdentifier);
            return old;
        });
    }

}
