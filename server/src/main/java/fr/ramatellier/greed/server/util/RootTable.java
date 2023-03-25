package fr.ramatellier.greed.server.util;

import fr.ramatellier.greed.server.Context;
import fr.ramatellier.greed.server.packet.FullPacket;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.function.Consumer;

/**
 * A Root table is a data structure that contains InetSocketAddress and their corresponding AddressContext.
 * A Key is an InetSocketAddress destination and the value is the InetSocketAddress of the closest neighbour to the destination and its context from this table.
 * This table can also perform action on the given neighbours registered in the table.
 */
public class RootTable {
    private final HashMap<InetSocketAddress, AddressContext> table = new HashMap<>();

    /**
     * Check if the table has registered the destination address.
     * @param socketAddress the address to check
     * @return true if the address is registered, false otherwise.
     */
    public boolean exists(InetSocketAddress socketAddress) {
        return table.containsKey(socketAddress);
    }

    /**
     * Put a new destination address in the table or update the value of an existing one.
     * @param destination the destination to registered or update
     * @param value the value of the entry
     * @param context the context of the entry
     */
    public void putOrUpdate(InetSocketAddress destination, InetSocketAddress value, Context context) {
        table.merge(destination, new AddressContext(value, context), (old, newValue) -> newValue);
    }

    /**
     * Transfer the given packet to the closest neighbour of the destination.
     * @param dst the destination of the packet
     * @param packet the packet to transfer
     */
    public void sendTo(InetSocketAddress dst, FullPacket packet){
        Objects.requireNonNull(dst);
        Objects.requireNonNull(packet);
        var neighbour = table.get(dst);
        if(neighbour == null) {
            return;
        }
        neighbour.context().queuePacket(packet);
    }

    /**
     * Get all the registered address in the table.
     * @return the set of neighbours
     */
    public Set<InetSocketAddress> registeredAddresses() {
        return table.keySet();
    }

    /**
     * Perform an action on all neighbours information except the one with the given address.
     * @param address the address to exclude, if the address is null then no neighbours will be ignored
     * @param action the action to perform on neighbours
     */
    public void onNeighboursDo(InetSocketAddress address, Consumer<AddressContext> action) {
        Objects.requireNonNull(action);
        for(var entry: table.entrySet()) {
            if(!isNeighbour(entry) || entry.getKey().equals(address)) {
                continue;
            }
            action.accept(entry.getValue());
        }
    }

    public List<AddressContext> allAddress() {
        return table.keySet().stream().map(k -> new AddressContext(k, table.get(k).context())).toList();
    }

    private boolean isNeighbour(Map.Entry<InetSocketAddress, AddressContext> entry){
        return entry.getKey().equals(entry.getValue().address());
    }

    @Override
    public String toString() {
        if(table.isEmpty()) return "Empty table";
        var builder = new StringBuilder();
        for(var value: table.entrySet()) {
            builder.append("Key: ").append(value.getKey()).append(" to ").append(value.getValue().address()).append("\n");
        }
        return builder.toString();
    }
}
