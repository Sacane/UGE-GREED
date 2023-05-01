package fr.ramatellier.greed.server.reader.sub;

import fr.ramatellier.greed.server.packet.component.IDComponent;
import fr.ramatellier.greed.server.packet.component.IDListComponent;
import fr.ramatellier.greed.server.reader.Reader;
import fr.ramatellier.greed.server.reader.primitive.IntReader;
import fr.ramatellier.greed.server.util.Buffers;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class IDListReader implements Reader<IDListComponent> {
    private enum State {
        DONE, WAITING_SIZE, WAITING_ID, ERROR
    }
    private final IntReader sizeReader = new IntReader();
    private final IDReader idReader = new IDReader();
    private final ArrayList<IDComponent> packetList = new ArrayList<>();
    private State state = State.WAITING_SIZE;
    @Override
    public ProcessStatus process(ByteBuffer buffer) {
        if(state == State.DONE || state == State.ERROR) {
            throw new IllegalStateException();
        }
        if(state == State.WAITING_SIZE) {
            Buffers.runOnProcess(buffer, sizeReader, __ -> state = State.WAITING_ID, () -> {}, () -> state = State.ERROR);
        }
        if(state == State.WAITING_ID) {
            for(int i = 0; i < sizeReader.get(); i++) {
                var status = idReader.process(buffer);
                if(status == ProcessStatus.DONE) {
                    packetList.add(idReader.get());
                    idReader.reset();
                } else if (status == ProcessStatus.ERROR) {
                    state = State.ERROR;
                    return ProcessStatus.ERROR;
                } else {
                    return ProcessStatus.REFILL;
                }
            }
            state = State.DONE;
        }
        if(state == State.DONE) {
            return ProcessStatus.DONE;
        }
        return ProcessStatus.REFILL;
    }

    @Override
    public IDListComponent get() {
        if(state != State.DONE) {
            throw new IllegalStateException();
        }
        return new IDListComponent(List.copyOf(packetList));
    }

    @Override
    public void reset() {
        state = State.WAITING_SIZE;
        sizeReader.reset();
        idReader.reset();
        packetList.clear();
    }
}
