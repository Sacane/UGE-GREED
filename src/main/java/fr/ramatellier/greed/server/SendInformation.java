package fr.ramatellier.greed.server;

import fr.ramatellier.greed.server.packet.full.FullPacket;

import java.net.InetSocketAddress;

public record SendInformation(InetSocketAddress address, FullPacket packet) {}
