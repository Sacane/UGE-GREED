package fr.ramatellier.greed.server.compute;

import java.util.Objects;

public final class ComputationEntity {
    private final ComputationIdentifier id;
    private final ComputeInfo info;
    private final long intendedUc;
    private long currentUcDone;

    public ComputationEntity(ComputationIdentifier id, ComputeInfo info) {
        this.id = Objects.requireNonNull(id);
        this.info = Objects.requireNonNull(info);
        this.intendedUc = info.end() - info.start();
    }

    public long remains() {
        return intendedUc - currentUcDone;
    }

    public void incrementUc() {
        if(currentUcDone >= intendedUc) {
            throw new IllegalStateException("Computation already done");
        }

        currentUcDone++;
    }

    public boolean isReady() {
        return currentUcDone >= intendedUc;
    }

    public ComputationIdentifier id() {
        return id;
    }

    public ComputeInfo info() {
        return info;
    }
}
