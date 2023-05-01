package fr.ramatellier.greed.server.packet.frame;

import fr.ramatellier.greed.server.util.OpCodes;


public record ConnectKOPacket() implements LocalFrame {
    @Override
    public OpCodes opCode() {
        return OpCodes.KO;
    }

}
