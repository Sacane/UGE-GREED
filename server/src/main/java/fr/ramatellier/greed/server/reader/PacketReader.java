package fr.ramatellier.greed.server.reader;

import fr.ramatellier.greed.server.packet.full.FullPacket;
import fr.ramatellier.greed.server.reader.primitive.ByteReader;
import fr.ramatellier.greed.server.util.OpCodes;
import fr.ramatellier.greed.server.util.TramKind;

import java.nio.ByteBuffer;
import java.util.HashMap;


public class PacketReader implements FullPacketReader {
    private enum State {
        DONE, WAITING_LOCATION, WAITING_CODE, WAITING_PACKET, ERROR
    }
    private State state = State.WAITING_LOCATION;
    private final HashMap<OpCodes, FullPacketReader> readers = createReaders();
    private final ByteReader locationReader = new ByteReader();
    private final ByteReader codeReader = new ByteReader();
    private FullPacket value;

    @Override
    public ProcessStatus process(ByteBuffer buffer) {
        if (state == State.DONE || state == State.ERROR) {
            throw new IllegalStateException();
        }

        if(state == State.WAITING_LOCATION) {
            var status = locationReader.process(buffer);

            if(status == ProcessStatus.DONE) {
                state = State.WAITING_CODE;
            }
        }
        if(state == State.WAITING_CODE) {
            var status = codeReader.process(buffer);

            if(status == ProcessStatus.DONE) {
                state = State.WAITING_PACKET;
            }
        }
        if(state == State.WAITING_PACKET) {
            var tramKind = TramKind.toTramKind(locationReader.get());
            if (tramKind == null) return ProcessStatus.ERROR;
            var opcode = OpCodes.fromByte(codeReader.get());
            if (opcode == null) return ProcessStatus.ERROR;
            var reader = readers.get(opcode);
            var status = reader.process(buffer);
            if (status == ProcessStatus.DONE) {
                state = State.DONE;
                value = reader.get();
            }
        }

        if (state != State.DONE) {
            return ProcessStatus.REFILL;
        }

        return Reader.ProcessStatus.DONE;
    }


    @Override
    public FullPacket get() {
        if (state != State.DONE) {
            throw new IllegalStateException();
        }
        return value;
    }

    @Override
    public void reset() {
        state = State.WAITING_LOCATION;
        locationReader.reset();
        codeReader.reset();
        resetReaders();
    }
    private static HashMap<OpCodes, FullPacketReader> createReaders(){
        var readers = new HashMap<OpCodes, FullPacketReader>();
        for(var opcode : OpCodes.values()){
            readers.put(opcode, opcode.reader());
        }
        return readers;
    }
    private void resetReaders(){
        for(var reader : readers.values()){
            if(reader != null){
                reader.reset();
            }
        }
    }
}
