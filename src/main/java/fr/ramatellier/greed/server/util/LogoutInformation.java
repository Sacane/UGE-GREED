package fr.ramatellier.greed.server.util;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Class that represents the information of a server if he wants to shut down
 */
public class LogoutInformation {
    private final InetSocketAddress address;
    private final List<InetSocketAddress> daughters;
    private final ArrayList<InetSocketAddress> received = new ArrayList<>();

    public LogoutInformation(InetSocketAddress address, List<InetSocketAddress> daughters) {
        Objects.requireNonNull(address);
        Objects.requireNonNull(daughters);
        this.address = address;
        this.daughters = List.copyOf(daughters);
    }

    /**
     * This method add an InetSocketAddress to the list of the addresses that asked for a connection
     *
     * @param address The InetSocketAddress that we received the connect request
     */
    public void add(InetSocketAddress address) {
        received.add(address);
    }

    /**
     * This method check if all the daughters have connected
     *
     * @return true if all the daughters have sent a connect request else false
     */
    public boolean allConnected() {
        return daughters.size() == received.size();
    }

    /**
     * This method return the address of the server that want to shut down
     *
     * @return The InetSocketAddress of the server that want to shut down
     */
    public InetSocketAddress getAddress() {
        return address;
    }
}
