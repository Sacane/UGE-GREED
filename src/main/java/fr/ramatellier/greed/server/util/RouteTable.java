package fr.ramatellier.greed.server.util;

import fr.ramatellier.greed.server.context.Context;
import fr.ramatellier.greed.server.frame.model.Frame;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.function.Consumer;

/**
 * A Root table is a data structure that contains InetSocketAddress and their corresponding AddressContext.
 * A Key is an InetSocketAddress destination and the value is the InetSocketAddress of the closest neighbour to the destination and its context from this table.
 * This table can also perform action on the given neighbours registered in the table.
 */
public class RouteTable {
    private final HashMap<InetSocketAddress, AddressContext> table = new HashMap<>();

    /**
     * Put a new destination address in the table or update the value of an existing one.
     *
     * @param destination the destination to registered or update
     * @param value the value of the entry
     * @param context the context of the entry
     */
    public void putOrUpdate(InetSocketAddress destination, InetSocketAddress value, Context context) {
        table.merge(destination, new AddressContext(value, context), (old, newValue) -> newValue);
    }

    /**
     * This method update the context of an address that already exist
     *
     * @param oldAddress The old InetSocketAddress
     * @param newAddress The new InetSocketAddress
     * @param context The new context
     */
    public void updateToContext(InetSocketAddress oldAddress, InetSocketAddress newAddress, Context context) {
        for(var entry: table.entrySet()) {
            if(oldAddress.equals(entry.getValue().address())) {
                putOrUpdate(entry.getKey(), newAddress, context);
            }
        }
    }

    /**
     * This method an InetSocketAddress from the table
     *
     * @param address The InetSocketAddress we want to remove
     */
    public void delete(InetSocketAddress address) {
        table.remove(address);
    }

    /**
     * Transfer the given packet to the closest neighbour of the destination.
     *
     * @param dst the destination of the packet
     * @param packet the packet to transfer
     */
    public void sendTo(InetSocketAddress dst, Frame packet) {
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
     *
     * @return the set of address
     */
    public Set<InetSocketAddress> registeredAddresses() {
        return table.keySet();
    }

    /**
     * This method will get all the ancestors of an InetSocketAddress
     *
     * @param parentAddress The InetSocketAddress of the parent
     * @return The list of the InetSocketAddress of all the ancestors
     */
    public List<InetSocketAddress> ancestors(InetSocketAddress parentAddress) {
        var ancestorsList = new ArrayList<InetSocketAddress>();

        for(var entry: table.entrySet()) {
            if(entry.getKey().equals(entry.getValue().address()) && !parentAddress.equals(entry.getKey())) {
                ancestorsList.add(entry.getKey());
                ancestorsList.addAll(ancestorsOf(entry.getKey()));
            }
        }
        return ancestorsList;
    }

    private List<InetSocketAddress> ancestorsOf(InetSocketAddress address) {
        var ancestors = new ArrayList<InetSocketAddress>();
        for(var entry: table.entrySet()) {
            if(address.equals(entry.getValue().address()) && !entry.getKey().equals(entry.getValue().address())) {
                ancestors.add(entry.getKey());
                ancestors.addAll(ancestorsOf(entry.getKey()));
            }
        }

        return ancestors;
    }

    /**
     * This method get the neighbors
     *
     * @return The list of the InetSocketAddress of the neighbors
     */
    public List<InetSocketAddress> neighbors() {
        var neighbors = new ArrayList<InetSocketAddress>();
        for(var entry: table.entrySet()) {
            if(entry.getKey().equals(entry.getValue().address())) {
                neighbors.add(entry.getKey());
            }
        }
        return neighbors;
    }

    /**
     * This method get the context of all the daughters
     *
     * @param parentAddress The InetSocketAddress of the parent
     * @return The list of the Context of the daughters
     */
    public List<Context> daughtersContext(InetSocketAddress parentAddress) {
        var daughters = new ArrayList<Context>();
        for(var entry: table.entrySet()) {
            if(entry.getKey().equals(entry.getValue().address()) && !parentAddress.equals(entry.getKey())) {
                daughters.add(entry.getValue().context());
            }
        }
        return daughters;
    }

    /**
     * Perform an action on all neighbours information except the one with the given address.
     *
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

    /**
     * This method return the size of the route table
     *
     * @return The size of the route table
     */
    public int size() {
        return table.size();
    }

    /**
     * This method allows to apply a Consumer to all the route table
     *
     * @param action The consumer we want to apply to everybody
     */
    public void performOnAllAddress(Consumer<AddressContext> action) {
        Objects.requireNonNull(action);
        table.keySet().stream().map(k -> new AddressContext(k, table.get(k).context())).forEach(action);
    }

    private boolean isNeighbour(Map.Entry<InetSocketAddress, AddressContext> entry) {
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
