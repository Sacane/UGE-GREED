package fr.ramatellier.greed.server.frame.model;

import fr.ramatellier.greed.server.frame.component.IDComponent;
import fr.ramatellier.greed.server.frame.component.IDListComponent;
import fr.ramatellier.greed.server.util.OpCodes;

public record LogoutRequestFrame(IDComponent id, IDListComponent daughters) implements LocalFrame {

    @Override
    public OpCodes opCode() {
        return OpCodes.LOGOUT_REQUEST;
    }
}
