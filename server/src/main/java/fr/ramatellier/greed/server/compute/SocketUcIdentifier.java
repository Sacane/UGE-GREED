package fr.ramatellier.greed.server.compute;

import java.net.InetSocketAddress;

public record SocketUcIdentifier(InetSocketAddress address, long uc) {
}
