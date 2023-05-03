package fr.ramatellier.greed.server.frame.model;

import fr.ramatellier.greed.server.frame.component.IDComponent;
import fr.ramatellier.greed.server.frame.component.primitive.LongComponent;

import java.util.Objects;

public record WorkRequestResponseFrame(IDComponent dst, IDComponent src, LongComponent requestID, LongComponent nb_uc) implements TransferFrame {
    public WorkRequestResponseFrame {
        Objects.requireNonNull(dst);
        Objects.requireNonNull(src);
    }
}
