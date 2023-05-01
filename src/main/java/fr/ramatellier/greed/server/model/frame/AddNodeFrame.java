package fr.ramatellier.greed.server.model.frame;

import fr.ramatellier.greed.server.model.component.IDComponent;
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
