package fr.ramatellier.greed.server.packet.frame;

import fr.ramatellier.greed.server.packet.component.IDComponent;
import fr.ramatellier.greed.server.util.OpCodes;

import java.util.Objects;

public record WorkRequestResponsePacket(IDComponent dst, IDComponent src, long requestID, long nb_uc) implements TransferFrame {

    public WorkRequestResponsePacket{
        Objects.requireNonNull(dst);
        Objects.requireNonNull(src);
    }

    @Override
    public OpCodes opCode() {
        return OpCodes.WORK_REQUEST_RESPONSE;
    }
}
