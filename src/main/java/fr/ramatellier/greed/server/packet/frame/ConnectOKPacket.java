package fr.ramatellier.greed.server.packet.frame;

import fr.ramatellier.greed.server.packet.component.IDComponent;
import fr.ramatellier.greed.server.packet.component.IDListComponent;
import fr.ramatellier.greed.server.util.OpCodes;

public record ConnectOKPacket(IDComponent idMother, IDListComponent neighbours) implements LocalFrame {


    public int getPort() {
        return idMother.getPort();
    }


    @Override
    public OpCodes opCode() {
        return OpCodes.OK;
    }

}
