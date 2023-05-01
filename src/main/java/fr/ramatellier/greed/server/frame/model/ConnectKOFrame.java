package fr.ramatellier.greed.server.frame.model;

import fr.ramatellier.greed.server.util.OpCodes;


public record ConnectKOFrame() implements LocalFrame {
    @Override
    public OpCodes opCode() {
        return OpCodes.KO;
    }

}
