package fr.ramatellier.greed.server.frame.model;

import fr.ramatellier.greed.server.frame.component.IDComponent;

import java.util.Objects;

public record WorkRequestResponseFrame(IDComponent dst, IDComponent src, long requestID, long nb_uc) implements TransferFrame {
    public WorkRequestResponseFrame {
        Objects.requireNonNull(dst);
        Objects.requireNonNull(src);
    }
}
