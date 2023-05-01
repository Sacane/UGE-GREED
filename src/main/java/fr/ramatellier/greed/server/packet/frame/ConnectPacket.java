package fr.ramatellier.greed.server.packet.frame;

import fr.ramatellier.greed.server.packet.component.IDComponent;
import fr.ramatellier.greed.server.util.OpCodes;

public record ConnectPacket(IDComponent idPacket) implements LocalFrame {


    @Override
    public OpCodes opCode() {
        return OpCodes.CONNECT;
    }

}
