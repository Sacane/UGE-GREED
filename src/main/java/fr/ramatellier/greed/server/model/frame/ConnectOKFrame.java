package fr.ramatellier.greed.server.model.frame;

import fr.ramatellier.greed.server.model.component.IDComponent;
import fr.ramatellier.greed.server.model.component.IDListComponent;
import fr.ramatellier.greed.server.util.OpCodes;

public record ConnectOKFrame(IDComponent idMother, IDListComponent neighbours) implements LocalFrame {


    public int getPort() {
        return idMother.getPort();
    }


    @Override
    public OpCodes opCode() {
        return OpCodes.OK;
    }

}
