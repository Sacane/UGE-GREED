package fr.ramatellier.greed.server.reader.full;

import fr.ramatellier.greed.server.packet.full.LogoutGrantedPacket;

public class LogoutGrantedPacketReader extends UncheckedFullPacketReader<LogoutGrantedPacket> {
    public LogoutGrantedPacketReader() {
        super(LogoutGrantedPacket::new);
    }
}
