package fr.ramatellier.greed.server.model.frame;

import fr.ramatellier.greed.server.model.component.IDComponent;
import fr.ramatellier.greed.server.util.OpCodes;

import java.util.Objects;

public record WorkRequestResponseFrame(IDComponent dst, IDComponent src, long requestID, long nb_uc) implements TransferFrame {

    public WorkRequestResponseFrame {
        Objects.requireNonNull(dst);
        Objects.requireNonNull(src);
    }

    @Override
    public OpCodes opCode() {
        return OpCodes.WORK_REQUEST_RESPONSE;
    }
}
