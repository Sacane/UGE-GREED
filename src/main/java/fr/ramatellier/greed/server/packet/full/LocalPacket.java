package fr.ramatellier.greed.server.packet.full;

import fr.ramatellier.greed.server.util.TramKind;

public sealed interface LocalPacket extends FullPacket permits ConnectKOPacket, ConnectOKPacket, ConnectPacket, LogoutDeniedPacket, LogoutGrantedPacket, LogoutRequestPacket, PleaseReconnectPacket, ReconnectPacket {
    default TramKind kind(){
        return TramKind.LOCAL;
    }
}
