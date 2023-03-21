package fr.ramatellier.greed.server.util;

import fr.ramatellier.greed.server.Context;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.function.Consumer;

public class RootTable {
    private final HashMap<InetSocketAddress, AddressContext> table = new HashMap<>();

    public boolean exists(InetSocketAddress socketAddress) {
        return table.containsKey(socketAddress);
    }

    public void putOrUpdate(InetSocketAddress key, InetSocketAddress value, Context context) {
        table.merge(key, new AddressContext(value, context), (old, newValue) -> newValue);
    }

    public AddressContext closestNeighbourOf(InetSocketAddress address) {
        if(!exists(address)) return null;
        return table.get(address);
    }

    public Set<InetSocketAddress> neighbours() {
        /*var set = new HashSet<InetSocketAddress>();

        for(var socket: table.keySet()) {
            System.out.println(socket);
            set.add(socket);
        }

        return set;*/
        return table.keySet();
        // return new HashSet<>(table.values().stream().map(AddressContext::address).toList());
    }

    public void onNeighbours(InetSocketAddress address, Consumer<AddressContext> action) {
        for(var value: table.entrySet()) {
            if(value.getKey().equals(value.getValue().address()) && !value.getKey().equals(address)) {
                action.accept(value.getValue());
            }
        }
    }

    @Override
    public String toString() {
        var builder = new StringBuilder();
        for(var value: table.entrySet()) {
            builder.append("Key: ").append(value.getKey()).append("  to  ").append(value.getValue().address()).append("\n");
        }
        return builder.toString();
    }
}
