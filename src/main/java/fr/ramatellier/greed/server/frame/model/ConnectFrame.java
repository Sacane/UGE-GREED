package fr.ramatellier.greed.server.frame.model;

import fr.ramatellier.greed.server.frame.component.IDComponent;
import fr.ramatellier.greed.server.util.OpCodes;

public record ConnectFrame(IDComponent idPacket) implements LocalFrame {


    @Override
    public OpCodes opCode() {
        return OpCodes.CONNECT;
    }

}
