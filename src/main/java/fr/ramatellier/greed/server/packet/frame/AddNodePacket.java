package fr.ramatellier.greed.server.packet.frame;

import fr.ramatellier.greed.server.packet.component.IDComponent;
import fr.ramatellier.greed.server.util.OpCodes;

public record AddNodePacket(IDComponent src, IDComponent daughter) implements BroadcastFrame {
    @Override
    public OpCodes opCode() {
        return OpCodes.ADD_NODE;
    }

    @Override
    public BroadcastFrame withNewSource(IDComponent newSrc) {
        return new AddNodePacket(newSrc, daughter);
    }
}
