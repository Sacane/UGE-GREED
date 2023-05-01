package fr.ramatellier.greed.server.model.frame;

import fr.ramatellier.greed.server.util.OpCodes;

public record LogoutDeniedFrame() implements LocalFrame {
    @Override
    public OpCodes opCode() {
        return OpCodes.LOGOUT_DENIED;
    }
}
