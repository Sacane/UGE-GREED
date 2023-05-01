package fr.ramatellier.greed.server.model.frame;

import fr.ramatellier.greed.server.model.component.IDComponent;
import fr.ramatellier.greed.server.util.OpCodes;

public record PleaseReconnectFrame(IDComponent id) implements LocalFrame {

    @Override
    public OpCodes opCode() {
        return OpCodes.PLEASE_RECONNECT;
    }
}
