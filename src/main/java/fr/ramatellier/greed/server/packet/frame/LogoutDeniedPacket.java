package fr.ramatellier.greed.server.packet.frame;

import fr.ramatellier.greed.server.util.OpCodes;

public record LogoutDeniedPacket() implements LocalFrame {
    @Override
    public OpCodes opCode() {
        return OpCodes.LOGOUT_DENIED;
    }
}
