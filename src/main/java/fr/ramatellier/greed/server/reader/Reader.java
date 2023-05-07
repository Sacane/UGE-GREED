package fr.ramatellier.greed.server.reader;

import java.nio.ByteBuffer;

public interface Reader<T> {
    enum ProcessStatus {
        DONE, REFILL, ERROR
    }

    /**
     * Process the buffer and return the status of the process
     * @param buffer the buffer to process
     * @return the status of the process
     */
    ProcessStatus process(ByteBuffer buffer);

    /**
     * Get the value of the reader
     * @return the value of the reader
     */
    T get();

    /**
     * Reset the reader to its initial state
     */
    void reset();
}
