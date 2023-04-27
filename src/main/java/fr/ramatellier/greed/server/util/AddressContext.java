package fr.ramatellier.greed.server.util;

import fr.ramatellier.greed.server.Context;
import fr.ramatellier.greed.server.ServerApplicationContext;

import java.net.InetSocketAddress;

public record AddressContext(InetSocketAddress address, Context context) {}
