package fr.ramatellier.greed.server.reader;

import fr.ramatellier.greed.server.packet.ConnectOKPacket;
import fr.ramatellier.greed.server.packet.IDPacket;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class ConnectOKPacketReader implements Reader<ConnectOKPacket> {
    private enum State {
        DONE, WAITING_IDMOTHER, WAITING_SIZEID, WAITING_IDS, ERROR
    }
    private State state = State.WAITING_IDMOTHER;
    private final IDReader idReader = new IDReader();
    private final IntReader sizeReader = new IntReader();
    private IDPacket idMother;
    private ArrayList<IDPacket> ids;
    private ConnectOKPacket value;

    @Override
    public ProcessStatus process(ByteBuffer buffer) {
        if (state == State.DONE || state == State.ERROR) {
            throw new IllegalStateException();
        }

        if(state == State.WAITING_IDMOTHER) {
            var status = idReader.process(buffer);

            if(status == ProcessStatus.DONE) {
                state = State.WAITING_SIZEID;
                idMother = idReader.get();
                idReader.reset();
            }
        }
        if(state == State.WAITING_SIZEID) {
            var status = sizeReader.process(buffer);

            if(status == ProcessStatus.DONE) {
                state = State.WAITING_IDS;

                ids = new ArrayList<>();
            }
        }
        if(state == State.WAITING_IDS) {
            if(ids.size() == sizeReader.get()) {
                value = new ConnectOKPacket(idMother.getSocket(), ids.stream().map(IDPacket::getSocket).collect(Collectors.toSet()));
                state = State.DONE;
            }
            else {
                while(buffer.hasRemaining() && ids.size() != sizeReader.get()) {
                    var status = idReader.process(buffer);

                    if(status == ProcessStatus.DONE) {
                        ids.add(idReader.get());
                        idReader.reset();
                    }

                    if(ids.size() == sizeReader.get()) {
                        value = new ConnectOKPacket(idMother.getSocket(), ids.stream().map(IDPacket::getSocket).collect(Collectors.toSet()));
                        state = State.DONE;
                    }
                }
            }
        }

        if (state != State.DONE) {
            return ProcessStatus.REFILL;
        }

        return Reader.ProcessStatus.DONE;
    }

    @Override
    public ConnectOKPacket get() {
        if (state != State.DONE) {
            throw new IllegalStateException();
        }

        return value;
    }

    @Override
    public void reset() {
        state = State.WAITING_IDMOTHER;
    }
}
