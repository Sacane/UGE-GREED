package fr.ramatellier.greed.server.frame.model;

import fr.ramatellier.greed.server.frame.component.IDComponent;
import fr.ramatellier.greed.server.frame.component.IDListComponent;
import fr.ramatellier.greed.server.util.OpCodes;

public record ReconnectFrame(IDComponent id, IDListComponent ancestors) implements LocalFrame {

    @Override
    public OpCodes opCode() {
        return OpCodes.RECONNECT;
    }

}
