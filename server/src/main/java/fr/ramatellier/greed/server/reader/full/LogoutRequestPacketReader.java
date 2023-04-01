package fr.ramatellier.greed.server.reader.full;

import fr.ramatellier.greed.server.packet.sub.IDPacket;
import fr.ramatellier.greed.server.packet.full.LogoutRequestPacket;
import fr.ramatellier.greed.server.reader.sub.IDReader;
import fr.ramatellier.greed.server.reader.Reader;
import fr.ramatellier.greed.server.reader.primitive.IntReader;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public class LogoutRequestPacketReader implements Reader<LogoutRequestPacket> {
    private enum State {
        DONE, WAITING_ID, WAITING_SIZE, WAITING_IDS, ERROR
    }
    private State state = State.WAITING_ID;
    private final IDReader idMother = new IDReader();
    private final IntReader sizeReader = new IntReader();
    private final IDReader idReader = new IDReader();
    private ArrayList<IDPacket> ids;
    private LogoutRequestPacket value;

    @Override
    public ProcessStatus process(ByteBuffer buffer) {
        if (state == State.DONE || state == State.ERROR) {
            throw new IllegalStateException();
        }

        if(state == State.WAITING_ID) {
            var status = idMother.process(buffer);

            if(status == ProcessStatus.DONE) {
                state = State.WAITING_SIZE;
            }
        }
        if(state == State.WAITING_SIZE) {
            var status = sizeReader.process(buffer);

            if(status == ProcessStatus.DONE) {
                state = State.WAITING_IDS;

                ids = new ArrayList<>();
            }
        }
        if(state == State.WAITING_IDS) {
            if(ids.size() == sizeReader.get()) {
                state = State.DONE;

                value = new LogoutRequestPacket(idMother.get().getSocket(), ids.stream().map(IDPacket::getSocket).toList());
            }

            while(buffer.limit() > 0 && ids.size() != sizeReader.get()) {
                var status = idReader.process(buffer);

                if(status == ProcessStatus.DONE) {
                    ids.add(idReader.get());
                    idReader.reset();
                }

                if(ids.size() == sizeReader.get()) {
                    state = State.DONE;

                    value = new LogoutRequestPacket(idMother.get().getSocket(), ids.stream().map(IDPacket::getSocket).toList());
                }
            }
        }

        if (state != State.DONE) {
            return ProcessStatus.REFILL;
        }

        return Reader.ProcessStatus.DONE;
    }

    @Override
    public LogoutRequestPacket get() {
        if (state != State.DONE) {
            throw new IllegalStateException();
        }

        return value;
    }

    @Override
    public void reset() {
        state = State.WAITING_ID;
        idMother.reset();
        sizeReader.reset();
        idReader.reset();
    }
}
