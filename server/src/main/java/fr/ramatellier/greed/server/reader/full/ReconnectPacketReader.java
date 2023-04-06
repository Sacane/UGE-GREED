package fr.ramatellier.greed.server.reader.full;

import fr.ramatellier.greed.server.packet.sub.IDPacket;
import fr.ramatellier.greed.server.packet.full.ReconnectPacket;
import fr.ramatellier.greed.server.reader.FullPacketReader;
import fr.ramatellier.greed.server.reader.sub.IDReader;
import fr.ramatellier.greed.server.reader.Reader;
import fr.ramatellier.greed.server.reader.primitive.IntReader;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public class ReconnectPacketReader implements FullPacketReader {
    private enum State {
        DONE, WAITING_ID, WAITING_SIZE, WAITING_IDS, ERROR
    }
    private State state = State.WAITING_ID;
    private final IDReader idReader = new IDReader();
    private final IntReader sizeReader = new IntReader();
    private final IDReader ancestorIdReader = new IDReader();
    private ArrayList<IDPacket> ids;
    private ReconnectPacket value;

    @Override
    public ProcessStatus process(ByteBuffer buffer) {
        if (state == State.DONE || state == State.ERROR) {
            throw new IllegalStateException();
        }

        if(state == State.WAITING_ID) {
            var status = idReader.process(buffer);

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
            fillList(ids, sizeReader.get(), ancestorIdReader, buffer);

            if(ids.size() == sizeReader.get()) {
                state = State.DONE;

                value = new ReconnectPacket(idReader.get().getSocket(), ids.stream().map(IDPacket::getSocket).toList());
            }
        }

        if (state != State.DONE) {
            return ProcessStatus.REFILL;
        }

        return Reader.ProcessStatus.DONE;
    }

    @Override
    public ReconnectPacket get() {
        if (state != State.DONE) {
            throw new IllegalStateException();
        }

        return value;
    }

    @Override
    public void reset() {
        state = State.WAITING_ID;
        idReader.reset();
        sizeReader.reset();
        ancestorIdReader.reset();
    }
}