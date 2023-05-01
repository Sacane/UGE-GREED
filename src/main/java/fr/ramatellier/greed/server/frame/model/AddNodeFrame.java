package fr.ramatellier.greed.server.frame.model;

import fr.ramatellier.greed.server.frame.component.IDComponent;
import fr.ramatellier.greed.server.util.OpCodes;

public record AddNodeFrame(IDComponent src, IDComponent daughter) implements BroadcastFrame {
    @Override
    public OpCodes opCode() {
        return OpCodes.ADD_NODE;
    }

    @Override
    public BroadcastFrame withNewSource(IDComponent newSrc) {
        return new AddNodeFrame(newSrc, daughter);
    }
}
