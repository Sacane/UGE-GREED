package fr.ramatellier.greed.server;

import java.net.InetSocketAddress;

public record Address(InetSocketAddress address, Context context) {}