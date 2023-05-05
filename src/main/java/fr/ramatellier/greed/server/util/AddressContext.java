package fr.ramatellier.greed.server.util;

import fr.ramatellier.greed.server.context.Context;

import java.net.InetSocketAddress;

public record AddressContext(InetSocketAddress address, Context context) {}
