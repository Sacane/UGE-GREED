package fr.ramatellier.greed.server.model.frame;

import fr.ramatellier.greed.server.model.component.IDComponent;
import fr.ramatellier.greed.server.model.component.ResponseComponent;
import fr.ramatellier.greed.server.util.OpCodes;

import java.util.Objects;

public record WorkResponseFrame(
    IDComponent src,
    IDComponent dst,
    long requestID,
    ResponseComponent responsePacket
) implements TransferFrame {
    public WorkResponseFrame {
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
