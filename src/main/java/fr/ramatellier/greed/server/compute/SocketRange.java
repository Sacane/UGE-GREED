package fr.ramatellier.greed.server.compute;

import java.net.InetSocketAddress;

public record SocketRange(InetSocketAddress socketAddress, Range range) {
}
