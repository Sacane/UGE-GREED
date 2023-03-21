package fr.ramatellier.greed.server.util;

import fr.ramatellier.greed.server.Context;

import java.net.InetSocketAddress;
import java.util.*;

public class RootTable {
    private final HashMap<InetSocketAddress, Informations> table = new HashMap<>();

    public boolean exists(InetSocketAddress socketAddress) {
        return table.containsKey(socketAddress);
    }

    public void putOrUpdate(InetSocketAddress key, InetSocketAddress value, Context context) {
        table.merge(key, new Informations(value, context), (old, newValue) -> newValue);
    }

    public Informations closestNeighbourOf(InetSocketAddress address) {
        if(!exists(address)) return null;

        return table.get(address);
    }

    public Set<InetSocketAddress> neighbours() {
        return new HashSet<>(table.values().stream().map(i -> i.getAddress()).toList());
    }

    public List<Context> onNeighbours(InetSocketAddress address) {
        var neighbours = new ArrayList<Context>();

        for(var value: table.entrySet()) {
            if(value.getKey().equals(value.getValue().getAddress()) && !value.getKey().equals(address)) {
                neighbours.add(value.getValue().getContext());
            }
        }

        return neighbours;
    }

    @Override
    public String toString() {
        var builder = new StringBuilder();
        for(var value: table.entrySet()) {
            builder.append("Key: ").append(value.getKey()).append("  to  ").append(value.getValue().getAddress()).append("\n");
        }
        return builder.toString();
    }
}
