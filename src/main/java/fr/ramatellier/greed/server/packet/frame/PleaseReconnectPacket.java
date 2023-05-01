package fr.ramatellier.greed.server.packet.frame;

import fr.ramatellier.greed.server.packet.component.IDComponent;
import fr.ramatellier.greed.server.util.OpCodes;

public record PleaseReconnectPacket(IDComponent id) implements LocalFrame {

    @Override
    public OpCodes opCode() {
        return OpCodes.PLEASE_RECONNECT;
    }
}
