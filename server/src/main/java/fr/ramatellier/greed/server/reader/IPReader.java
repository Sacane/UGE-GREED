package fr.ramatellier.greed.server.reader;

import fr.ramatellier.greed.server.IPPacket;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class IPReader implements Reader<IPPacket> {
    private enum State {
        DONE, WAITING_SIZE, WAITING_ADDRESS, ERROR
    }
    private State state = State.WAITING_SIZE;
    private final ByteReader sizeReader = new ByteReader();
    private ByteBuffer addressBuffer;
    private IPPacket value;

    @Override
    public ProcessStatus process(ByteBuffer buffer) {
        if (state == State.DONE || state == State.ERROR) {
            throw new IllegalStateException();
        }

        if(state == State.WAITING_SIZE) {
            var status = sizeReader.process(buffer);

            if(status == ProcessStatus.DONE) {
                state = State.WAITING_ADDRESS;

                if(sizeReader.get() == 4) {
                    addressBuffer = ByteBuffer.allocate(4);
                }
                else {
                    addressBuffer = ByteBuffer.allocate(16);
                }
            }
        }
        if(state == State.WAITING_ADDRESS) {
            fillBuffer(buffer, addressBuffer);

            if(!addressBuffer.hasRemaining()) {
                addressBuffer.flip();
                String address;
                try {
                    var bytes = new byte[addressBuffer.remaining()];
                    var addres = InetAddress.getByAddress(bytes);
                    value = new IPPacket(addres.getHostAddress());
                } catch (UnknownHostException e) {
                    throw new RuntimeException(e);
                }
//                if(sizeReader.get() == 4) {
//                    var values = new ArrayList<Byte>();
//
//                    for(var i = 0; i < 4; i++) {
//                        values.add(addressBuffer.get());
//                    }
//
//                    address = values.stream().map(s -> s.toString()).collect(Collectors.joining("."));
//                }
//                else {
//                    var values = new ArrayList<Short>();
//
//                    for(var i = 0; i < 8; i++) {
//                        values.add(addressBuffer.getShort());
//                    }
//
//                    address = values.stream().map(s -> s.toString()).collect(Collectors.joining(":"));
//                }
                state = State.DONE;
//                value = new IPPacket(address);
            }
        }

        if (state != State.DONE) {
            return ProcessStatus.REFILL;
        }

        return ProcessStatus.DONE;
    }

    @Override
    public IPPacket get() {
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
