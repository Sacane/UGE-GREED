package fr.ramatellier.greed.server.frame.model;

import fr.ramatellier.greed.server.frame.component.IDComponent;
import fr.ramatellier.greed.server.frame.component.ResponseComponent;
import fr.ramatellier.greed.server.frame.component.primitive.LongComponent;

import java.util.Objects;

public record WorkResponseFrame(
    IDComponent src,
    IDComponent dst,
    LongComponent requestID,
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
