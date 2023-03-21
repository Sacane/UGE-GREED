package fr.ramatellier.greed.server.util;

import fr.ramatellier.greed.server.Context;

import java.net.InetSocketAddress;

public class Informations {
    private final InetSocketAddress address;
    private final Context context;

    public Informations(InetSocketAddress address, Context context) {
        this.address = address;
        this.context = context;
    }

    public InetSocketAddress getAddress() {
        return address;
    }

    public Context getContext() {
        return context;
    }
}
