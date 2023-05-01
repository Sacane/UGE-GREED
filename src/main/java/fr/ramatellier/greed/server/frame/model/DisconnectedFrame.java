package fr.ramatellier.greed.server.frame.model;

import fr.ramatellier.greed.server.frame.component.IDComponent;
import fr.ramatellier.greed.server.util.OpCodes;

public record DisconnectedFrame(IDComponent src, IDComponent id) implements BroadcastFrame {


    @Override
    public BroadcastFrame withNewSource(IDComponent newSrc) {
        return new DisconnectedFrame(newSrc, id);
    }

    @Override
    public OpCodes opCode() {
        return OpCodes.DISCONNECTED;
    }
}
