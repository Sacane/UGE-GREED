package fr.ramatellier.greed.server.reader.full;

import fr.ramatellier.greed.server.packet.full.ConnectKOPacket;

public class ConnectKOPacketReader extends UncheckedFullPacketReader<ConnectKOPacket> {
    public ConnectKOPacketReader() {
        super(ConnectKOPacket.class);
    }
}
