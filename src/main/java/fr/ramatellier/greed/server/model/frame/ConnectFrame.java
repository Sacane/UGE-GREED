package fr.ramatellier.greed.server.model.frame;

import fr.ramatellier.greed.server.model.component.IDComponent;
import fr.ramatellier.greed.server.util.OpCodes;

public record ConnectFrame(IDComponent idPacket) implements LocalFrame {


    @Override
    public OpCodes opCode() {
        return OpCodes.CONNECT;
    }

}
