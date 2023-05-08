package fr.ramatellier.greed.server.reader.component;

import fr.ramatellier.greed.server.frame.component.ResponseComponent;
import fr.ramatellier.greed.server.reader.Buffers;
import fr.ramatellier.greed.server.reader.Reader;
import fr.ramatellier.greed.server.reader.component.primitive.ByteComponentReader;
import fr.ramatellier.greed.server.reader.component.primitive.LongComponentReader;

import java.nio.ByteBuffer;

public class ResponseComponentReader implements Reader<ResponseComponent> {
    enum State {
        DONE, WAITING_VALUE, WAITING_RESPONSE_CODE, WAITING_RESPONSE, ERROR
    }
    private State state = State.WAITING_VALUE;
    private final LongComponentReader valueReader = new LongComponentReader();
    private final ByteComponentReader codeReader = new ByteComponentReader();
    private final StringReader responseReader = new StringReader();
    private ResponseComponent value;

    @Override
    public ProcessStatus process(ByteBuffer buffer) {
        if(state == State.DONE || state == State.ERROR) {
            throw new IllegalStateException();
        }

        if(state == State.WAITING_VALUE) {
            Buffers.runOnProcess(
                    buffer,
                    valueReader,
                    __ -> state = State.WAITING_RESPONSE_CODE,
                    () -> state = State.ERROR
            );
        }
        if(state == State.WAITING_RESPONSE_CODE) {
            Buffers.runOnProcess(
                    buffer,
                    codeReader,
                    __ -> state = State.WAITING_RESPONSE,
                    () -> state = State.ERROR
            );
        }
        if(state == State.WAITING_RESPONSE) {
            Buffers.runOnProcess(
                    buffer,
                    responseReader,
                    response -> {
                        state = State.DONE;
                        value = new ResponseComponent(valueReader.get().get(), response.value(), codeReader.get().get());
                    },
                    () -> state = State.ERROR
            );
        }
        if(state != State.DONE) {
            return ProcessStatus.REFILL;
        }
        return ProcessStatus.DONE;
    }

    @Override
    public ResponseComponent get() {
        if(state != State.DONE) {
            throw new IllegalStateException();
        }

        return value;
    }

    @Override
    public void reset() {
        state = State.WAITING_VALUE;
        valueReader.reset();
        codeReader.reset();
        responseReader.reset();
    }
}
