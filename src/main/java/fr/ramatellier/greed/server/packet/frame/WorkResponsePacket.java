package fr.ramatellier.greed.server.packet.frame;

import fr.ramatellier.greed.server.packet.component.IDComponent;
import fr.ramatellier.greed.server.packet.component.ResponseComponent;
import fr.ramatellier.greed.server.util.OpCodes;

import java.util.Objects;

public record WorkResponsePacket(
    IDComponent src,
    IDComponent dst,
    long requestID,
    ResponseComponent responsePacket
) implements TransferFrame {
    public WorkResponsePacket{
        Objects.requireNonNull(src);
        Objects.requireNonNull(dst);
        Objects.requireNonNull(responsePacket);
    }

    @Override
    public OpCodes opCode() {
        return OpCodes.WORK_RESPONSE;
    }

    public String result(){
        return responsePacket.getResponse().value();
    }
}
