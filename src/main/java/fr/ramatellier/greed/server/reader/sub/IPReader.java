package fr.ramatellier.greed.server.reader.sub;

import fr.ramatellier.greed.server.packet.sub.IpAddressPacket;
import fr.ramatellier.greed.server.reader.Reader;
import fr.ramatellier.greed.server.reader.primitive.ByteReader;
import fr.ramatellier.greed.server.util.Buffers;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class IPReader implements Reader<IpAddressPacket> {
    private enum State {
        DONE, WAITING_SIZE, WAITING_ADDRESS, ERROR
    }
    private State state = State.WAITING_SIZE;
    private final ByteReader sizeReader = new ByteReader();
    private ByteBuffer addressBuffer;
    private IpAddressPacket value;

    @Override
    public ProcessStatus process(ByteBuffer buffer) {
        if (state == State.DONE || state == State.ERROR) {
            throw new IllegalStateException();
        }

        if(state == State.WAITING_SIZE) {
            Buffers.runOnProcess(buffer, sizeReader,
                    result -> {
                        state = State.WAITING_ADDRESS;
                        addressBuffer = ByteBuffer.allocate(result == 4 ? 4 : 16);
                    },
                    () -> {},
                    () -> state = State.ERROR);
//            var status = sizeReader.process(buffer);
//
//            if(status == ProcessStatus.DONE) {
//                state = State.WAITING_ADDRESS;
//
//                addressBuffer = ByteBuffer.allocate(sizeReader.get() == 4 ? 4 : 16);
//            }
        }
        if(state == State.WAITING_ADDRESS) {
            Buffers.fillBuffer(buffer, addressBuffer);

            if(!addressBuffer.hasRemaining()) {
                addressBuffer.flip();
                try {
                    var bytes = new byte[addressBuffer.remaining()];
                    var address = InetAddress.getByAddress(bytes);
                    value = new IpAddressPacket(address.getHostAddress());
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
    public IpAddressPacket get() {
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
