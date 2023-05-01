package fr.ramatellier.greed.server.model.frame;

import fr.ramatellier.greed.server.util.OpCodes;

public record LogoutGrantedFrame() implements LocalFrame {
    @Override
    public OpCodes opCode() {
        return OpCodes.LOGOUT_GRANTED;
    }
}
