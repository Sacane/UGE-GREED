package fr.ramatellier.greed.server.model.frame;

import fr.ramatellier.greed.server.model.component.IDComponent;
import fr.ramatellier.greed.server.model.component.IDListComponent;
import fr.ramatellier.greed.server.util.OpCodes;

public record LogoutRequestFrame(IDComponent id, IDListComponent daughters) implements LocalFrame {

    @Override
    public OpCodes opCode() {
        return OpCodes.LOGOUT_REQUEST;
    }
}
