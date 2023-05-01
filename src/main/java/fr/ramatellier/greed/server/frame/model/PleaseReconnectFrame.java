package fr.ramatellier.greed.server.frame.model;

import fr.ramatellier.greed.server.frame.component.IDComponent;
import fr.ramatellier.greed.server.util.OpCodes;

public record PleaseReconnectFrame(IDComponent id) implements LocalFrame {

    @Override
    public OpCodes opCode() {
        return OpCodes.PLEASE_RECONNECT;
    }
}
