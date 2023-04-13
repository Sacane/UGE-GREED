package fr.ramatellier.greed.server.reader.full;

import fr.ramatellier.greed.server.packet.full.ConnectOKPacket;
import fr.ramatellier.greed.server.packet.sub.IDPacket;
import fr.ramatellier.greed.server.packet.sub.IDPacketList;
import fr.ramatellier.greed.server.reader.FullPacketReader;
import fr.ramatellier.greed.server.reader.sub.IDReader;
import fr.ramatellier.greed.server.reader.Reader;
import fr.ramatellier.greed.server.reader.primitive.IntReader;
import fr.ramatellier.greed.server.util.Buffers;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class ConnectOKPacketReader implements FullPacketReader {
    private enum State {
        DONE, WAITING_ID, WAITING_SIZE, WAITING_IDS, ERROR
    }
    private State state = State.WAITING_ID;
    private final IDReader idMotherReader = new IDReader();
    private final IntReader sizeReader = new IntReader();
    private final IDReader idReader = new IDReader();
    private ArrayList<IDPacket> ids;
    private ConnectOKPacket value;

    @Override
    public ProcessStatus process(ByteBuffer buffer) {
        if (state == State.DONE || state == State.ERROR) {
            throw new IllegalStateException();
        }
        if(state == State.WAITING_ID) {
            var status = idMotherReader.process(buffer);

            if(status == ProcessStatus.DONE) {
                state = State.WAITING_SIZE;
                System.out.println(status);
            }
        }
        if(state == State.WAITING_SIZE) {
            var status = sizeReader.process(buffer);
            System.out.println("SIZE : " + sizeReader.get());
            if(status == ProcessStatus.DONE) {
                state = State.WAITING_IDS;
                System.out.println(status);
                ids = new ArrayList<>();
            }
        }
        if(state == State.WAITING_IDS) {
            Buffers.fillList(ids, sizeReader.get(), idReader, buffer);
            System.out.println("SIZE: " + sizeReader.get() + " IDS: " + ids.size());
            if(ids.size() == sizeReader.get()) {
                System.out.println("READ");
                state = State.DONE;

                value = new ConnectOKPacket(idMotherReader.get(), new IDPacketList(ids.stream().toList()));
            }
            System.out.println("SIZE: " + sizeReader.get() + " IDS: " + ids.size());
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
        state = State.WAITING_ID;
        idMotherReader.reset();
        sizeReader.reset();
        idReader.reset();
    }
}
