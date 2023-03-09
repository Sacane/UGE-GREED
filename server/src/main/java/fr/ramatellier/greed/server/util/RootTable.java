package fr.ramatellier.greed.server.util;

import java.net.InetSocketAddress;
import java.util.HashMap;

public class RootTable {
    private final HashMap<InetSocketAddress, InetSocketAddress> table = new HashMap<>();
    public boolean exists(InetSocketAddress socketAddress){
        return table.containsKey(socketAddress);
    }
    public void putOrUpdate(InetSocketAddress key, InetSocketAddress value){
        table.merge(key, value, (old, newValue) -> newValue);
    }
    public InetSocketAddress getNeighbourOf(InetSocketAddress address){
        if(!exists(address)) return null;
        return table.get(address);
    }
}
