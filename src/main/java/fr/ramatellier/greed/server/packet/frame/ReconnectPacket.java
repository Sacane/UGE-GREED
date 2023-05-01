package fr.ramatellier.greed.server.packet.frame;

import fr.ramatellier.greed.server.packet.component.IDComponent;
import fr.ramatellier.greed.server.packet.component.IDListComponent;
import fr.ramatellier.greed.server.util.OpCodes;

public record ReconnectPacket(IDComponent id, IDListComponent ancestors) implements LocalFrame {

    @Override
    public OpCodes opCode() {
        return OpCodes.RECONNECT;
    }

}
