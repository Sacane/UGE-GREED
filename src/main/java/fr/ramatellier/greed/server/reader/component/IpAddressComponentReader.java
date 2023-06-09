package fr.ramatellier.greed.server.reader.component;

import fr.ramatellier.greed.server.frame.component.IpAddressComponent;
import fr.ramatellier.greed.server.reader.Buffers;
import fr.ramatellier.greed.server.reader.Reader;
import fr.ramatellier.greed.server.reader.component.primitive.ByteComponentReader;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class IpAddressComponentReader implements Reader<IpAddressComponent> {
    private enum State {
        DONE, WAITING_SIZE, WAITING_ADDRESS, ERROR
    }
    private State state = State.WAITING_SIZE;
    private final ByteComponentReader sizeReader = new ByteComponentReader();
    private ByteBuffer addressBuffer;
    private IpAddressComponent value;

    @Override
    public ProcessStatus process(ByteBuffer buffer) {
        if (state == State.DONE || state == State.ERROR) {
            throw new IllegalStateException();
        }

        if(state == State.WAITING_SIZE) {
            Buffers.runOnProcess(buffer, sizeReader,
                    result -> {
                        state = State.WAITING_ADDRESS;
                        addressBuffer = ByteBuffer.allocate(result.get() == 4 ? 4 : 16);
                    },
                    () -> state = State.ERROR);
        }
        if(state == State.WAITING_ADDRESS) {
            Buffers.fillBuffer(buffer, addressBuffer);

            if(!addressBuffer.hasRemaining()) {
                addressBuffer.flip();
                try {
                    var bytes = new byte[addressBuffer.remaining()];
                    var address = InetAddress.getByAddress(bytes);
                    value = new IpAddressComponent(address.getHostAddress());
                    state = State.DONE;
                } catch (UnknownHostException e) {
                    return ProcessStatus.ERROR;
                }
            }
        }

        if (state != State.DONE) {
            return ProcessStatus.REFILL;
        }

        return ProcessStatus.DONE;
    }

    @Override
    public IpAddressComponent get() {
        if (state != State.DONE) {
            throw new IllegalStateException();
        }

        return value;
    }

    @Override
    public void reset() {
        state = State.WAITING_SIZE;
        sizeReader.reset();
    }
}
