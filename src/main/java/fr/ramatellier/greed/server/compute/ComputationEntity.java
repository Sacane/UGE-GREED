package fr.ramatellier.greed.server.compute;

import java.util.Objects;

/**
 * Represent a computation entity.
 * A computation entity is identified by a {@link ComputationIdentifier}.
 * An entity keep track on its possibility to be computed or not. It also knows how many unit of computation it has to do and
 * how many of them are successfully done.
 */
public final class ComputationEntity {
    private final ComputationIdentifier id;
    private final ComputeInfo info;
    private long intendedUc;
    private long currentUcDone;
    private long nbUcOk;

    public ComputationEntity(ComputationIdentifier id, ComputeInfo info) {
        this.id = Objects.requireNonNull(id);
        this.info = Objects.requireNonNull(info);
        this.intendedUc = info.end() - info.start();
    }

    public void setRange(long start, long end) {
        intendedUc = end - start;
    }


    /**
     * @return the number of unit of computation that remains to be done
     */
    public long remains() {
        return intendedUc - currentUcDone;
    }

    /**
     * Increment the number of unit of computation that has been done.
     */
    public void incrementUc(boolean isOk) {
        if(currentUcDone >= intendedUc) {
            throw new IllegalStateException("Computation already done");
        }
        currentUcDone++;
        if(isOk) {
            nbUcOk++;
        }
    }

    /**
     * Check if the computation is ready to be computed.
     * @return true if the computation is ready to be computed, false otherwise
     */
    public boolean isReady() {
        return currentUcDone >= intendedUc;
    }

    /**
     * @return the identifier of this computation entity.
     */
    public ComputationIdentifier id() {
        return id;
    }
    /**
     * @return the information about this computation entity.
     */
    public ComputeInfo info() {
        return info;
    }
    public Range deltaOkResponse(){
        return new Range(nbUcOk, currentUcDone);
    }
}
