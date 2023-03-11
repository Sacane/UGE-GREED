package fr.ramatellier.greed.server.util;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class RootTable {
    private final HashMap<InetSocketAddress, InetSocketAddress> table = new HashMap<>();

    public boolean exists(InetSocketAddress socketAddress) {
        return table.containsKey(socketAddress);
    }

    public void putOrUpdate(InetSocketAddress key, InetSocketAddress value) {
        table.merge(key, value, (old, newValue) -> newValue);
    }

    public InetSocketAddress getNeighbourOf(InetSocketAddress address) {
        if(!exists(address)) return null;
        return table.get(address);
    }

    public List<InetSocketAddress> keys() {
        return table.keySet().stream().toList();
    }

    @Override
    public String toString() {
        var res = "";

        for(var value: table.entrySet()) {
            res += "Key: " + value.getKey() + "  to  " + value.getValue() + "\n";
        }

        return res;
    }
}
