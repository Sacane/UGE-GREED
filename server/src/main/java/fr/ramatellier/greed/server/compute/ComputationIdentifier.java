package fr.ramatellier.greed.server.compute;

import java.net.InetSocketAddress;

public record ComputationIdentifier(long id, InetSocketAddress address) {}
