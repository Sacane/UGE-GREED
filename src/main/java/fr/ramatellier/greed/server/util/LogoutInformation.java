package fr.ramatellier.greed.server.util;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class LogoutInformation {
    private final InetSocketAddress address;
    private final List<InetSocketAddress> daughters;
    private final ArrayList<InetSocketAddress> received;

    public LogoutInformation(InetSocketAddress address, List<InetSocketAddress> daughters) {
        this.address = address;
        this.daughters = daughters;
        received = new ArrayList<>();
    }

    public void add(InetSocketAddress address) {
        received.add(address);
    }

    public boolean allConnected() {
        return daughters.size() == received.size();
    }

    public InetSocketAddress getAddress() {
        return address;
    }
}
