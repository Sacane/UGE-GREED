package fr.ramatellier.greed.server.compute;

import fr.ramatellier.greed.server.ComputeInfo;

import java.util.Objects;

public record ComputationEntity(ComputationIdentifier id, ComputeInfo info) {
    public ComputationEntity {
        Objects.requireNonNull(id);
        Objects.requireNonNull(info);
    }
}