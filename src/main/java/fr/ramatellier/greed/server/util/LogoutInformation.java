package fr.ramatellier.greed.server.util;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
