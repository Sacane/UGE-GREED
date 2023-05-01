package fr.ramatellier.greed.server.model.frame;

import fr.ramatellier.greed.server.model.component.IDComponent;
import fr.ramatellier.greed.server.model.component.IDListComponent;
import fr.ramatellier.greed.server.util.OpCodes;

public record ReconnectFrame(IDComponent id, IDListComponent ancestors) implements LocalFrame {

    @Override
    public OpCodes opCode() {
        return OpCodes.RECONNECT;
    }

}
