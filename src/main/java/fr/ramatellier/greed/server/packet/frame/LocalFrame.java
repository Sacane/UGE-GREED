package fr.ramatellier.greed.server.packet.frame;

import fr.ramatellier.greed.server.util.FrameKind;

public sealed interface LocalFrame extends Frame permits ConnectKOPacket, ConnectOKPacket, ConnectPacket, LogoutDeniedPacket, LogoutGrantedPacket, LogoutRequestPacket, PleaseReconnectPacket, ReconnectPacket {
    default FrameKind kind(){
        return FrameKind.LOCAL;
    }
}
