package fr.ramatellier.greed.server.packet.frame;

import fr.ramatellier.greed.server.packet.component.IDComponent;
import fr.ramatellier.greed.server.util.OpCodes;

public record DisconnectedPacket(IDComponent src, IDComponent id) implements BroadcastFrame {


    @Override
    public BroadcastFrame withNewSource(IDComponent newSrc) {
        return new DisconnectedPacket(newSrc, id);
    }

    @Override
    public OpCodes opCode() {
        return OpCodes.DISCONNECTED;
    }
}
