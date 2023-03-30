package fr.ramatellier.greed.server.compute;

import fr.ramatellier.greed.server.ComputeInfo;

import java.util.Objects;

public final class ComputationEntity {

    private final ComputeInfo info;
    private final long id;

    public ComputationEntity(long id, ComputeInfo info) {
        this.id = id;
        this.info = Objects.requireNonNull(info);
    }

    public ComputeInfo getInfo() {
        return info;
    }

    public long getId() {
        return id;
    }
}