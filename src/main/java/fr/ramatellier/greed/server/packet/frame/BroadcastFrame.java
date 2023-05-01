package fr.ramatellier.greed.server.packet.frame;

import fr.ramatellier.greed.server.packet.component.IDComponent;
import fr.ramatellier.greed.server.util.FrameKind;

public sealed interface BroadcastFrame extends Frame permits AddNodePacket, DisconnectedPacket {
    @Override
    default FrameKind kind(){
        return FrameKind.BROADCAST;
    }
    IDComponent src();
    BroadcastFrame withNewSource(IDComponent newSrc);
}
