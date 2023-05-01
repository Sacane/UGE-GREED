package fr.ramatellier.greed.server.frame.model;

import fr.ramatellier.greed.server.util.OpCodes;

public record LogoutDeniedFrame() implements LocalFrame {
    @Override
    public OpCodes opCode() {
        return OpCodes.LOGOUT_DENIED;
    }
}
