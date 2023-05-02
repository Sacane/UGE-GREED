package fr.ramatellier.greed.server.frame.model;

import fr.ramatellier.greed.server.frame.component.IDComponent;
import fr.ramatellier.greed.server.frame.component.ResponseComponent;

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
    public String result(){
        return responsePacket.getResponse().value();
    }
}
