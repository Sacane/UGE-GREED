package fr.ramatellier.greed.server.compute;

import java.util.Objects;

public record ComputationEntity(ComputationIdentifier id, ComputeInfo info) {
    public ComputationEntity {
        Objects.requireNonNull(id);
        Objects.requireNonNull(info);
    }
}