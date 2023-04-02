package fr.ramatellier.greed.server.reader.full;

import fr.ramatellier.greed.server.packet.full.LogoutDeniedPacket;
import fr.ramatellier.greed.server.reader.full.UncheckedFullPacketReader;

public class LogoutDeniedPacketReader extends UncheckedFullPacketReader<LogoutDeniedPacket> {
    public LogoutDeniedPacketReader() {
        super(LogoutDeniedPacket.class);
    }
}
