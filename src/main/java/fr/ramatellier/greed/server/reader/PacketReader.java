package fr.ramatellier.greed.server.reader;

import fr.ramatellier.greed.server.packet.Packet;
import fr.ramatellier.greed.server.packet.full.*;
import fr.ramatellier.greed.server.packet.sub.*;
import fr.ramatellier.greed.server.reader.primitive.ByteReader;
import fr.ramatellier.greed.server.reader.sub.*;
import fr.ramatellier.greed.server.util.OpCodes;
import fr.ramatellier.greed.server.util.TramKind;

import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class PacketReader implements FullPacketReader {
    private enum State {
        DONE, WAITING_LOCATION, WAITING_CODE, WAITING_PACKET, ERROR
    }
    private final MultiReader multiReader = new MultiReader();
    private State state = State.WAITING_LOCATION;
    private final ByteReader locationReader = new ByteReader();
    private final ByteReader codeReader = new ByteReader();
    private final StringReader stringReader = new StringReader();
    private final IDReader idReader = new IDReader();
    private final IPReader ipReader = new IPReader();
    private final CheckerPacketReader checkerPacketReader = new CheckerPacketReader();
    private final RangePacketReader rangePacketReader = new RangePacketReader();
    private final DestinationPacketReader destinationPacketReader = new DestinationPacketReader();
    private final ResponsePacketReader responsePacketReader = new ResponsePacketReader();
    private final IDListReader idListReader = new IDListReader();

    
    private final HashMap<OpCodes, FullPacketReader> readers = createReaders();
    private FullPacket value;

    @Override
    public ProcessStatus process(ByteBuffer buffer) {
        System.out.println("PacketReader state : " + state);
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
//            var reader = readers.get(opcode);
//            var status = reader.process(buffer);
//            if (status == ProcessStatus.DONE) {
//                state = State.DONE;
//                value = reader.get();
//            }
            try {
                var status = multiReader.process(buffer, opcode);
                if(status == ProcessStatus.DONE) {
                    state = State.DONE;
                    value = multiReader.get();
                }
            } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
                System.err.println("Error while creating reader for opcode " + opcode + " : " + e.getMessage());
                return ProcessStatus.ERROR;
            }
        }

        if (state != State.DONE) {
            return ProcessStatus.REFILL;
        }

        return ProcessStatus.DONE;
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
        multiReader.reset();
    }

    private static HashMap<OpCodes, FullPacketReader> createReaders() {
        var readers = new HashMap<OpCodes, FullPacketReader>();
        for(var opcode : OpCodes.values()){
            readers.put(opcode, opcode.reader());
        }
        return readers;
    }

    private void resetReaders() {
        readers.values().forEach(Reader::reset);
        checkerPacketReader.reset();
        rangePacketReader.reset();
        destinationPacketReader.reset();
        responsePacketReader.reset();
        idListReader.reset();
        stringReader.reset();
        idReader.reset();
        ipReader.reset();
    }
}
