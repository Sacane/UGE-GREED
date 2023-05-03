package fr.ramatellier.greed.server.reader;

import fr.ramatellier.greed.server.frame.component.*;
import fr.ramatellier.greed.server.frame.component.primitive.ByteComponent;
import fr.ramatellier.greed.server.frame.component.primitive.IntComponent;
import fr.ramatellier.greed.server.frame.component.primitive.LongComponent;
import fr.ramatellier.greed.server.frame.model.*;
import fr.ramatellier.greed.server.reader.component.*;
import fr.ramatellier.greed.server.reader.primitive.ByteReader;
import fr.ramatellier.greed.server.reader.primitive.IntReader;
import fr.ramatellier.greed.server.reader.primitive.LongReader;
import fr.ramatellier.greed.server.util.OpCode;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Thread-safe class used to decode any kind of {@link Frame} from a ByteBuffer.
 * It uses the {@link OpCode} to know which Packet to create.
 */
public class FrameReaderDecoder {
    private record PacketComponents(Class<? extends Frame> packet, Class<?>[] components){}
    private static final Map<OpCode, PacketComponents> opCodeToConstructors = new HashMap<>();
    private static final Map<Class<?>, Reader<?>> packetToReader = initPacketReader();
    private final ReentrantLock lock = new ReentrantLock();
    private Frame value;
    private int currentPosition;
    private final ArrayList<Object> currentValues = new ArrayList<>();
    private enum State{
        DONE, WAITING_PAYLOAD, ERROR
    }
    private State state = State.WAITING_PAYLOAD;
    private static Map<Class<?>, Reader<?>> initPacketReader(){
        var packetToReader = new HashMap<Class<?>, Reader<?>>();
        packetToReader.put(StringComponent.class, new StringReader());
        packetToReader.put(IDComponent.class, new IDComponentReader());
        packetToReader.put(IpAddressComponent.class, new IpAddressComponentReader());
        packetToReader.put(CheckerComponent.class, new CheckerComponentReader());
        packetToReader.put(RangeComponent.class, new RangeComponentReader());
        packetToReader.put(DestinationPacket.class, new DestinationComponentReader());
        packetToReader.put(ResponseComponent.class, new ResponseComponentReader());
        packetToReader.put(IDListComponent.class, new IDComponentListReader());
        packetToReader.put(LongComponent.class, new LongComponentReader());
        packetToReader.put(IntComponent.class, new IntComponentReader());
        packetToReader.put(ByteComponent.class, new ByteComponentReader());
        packetToReader.put(long.class, new LongReader());
        packetToReader.put(Integer.class, new IntReader());
        packetToReader.put(Byte.class, new ByteReader());

        return packetToReader;
    }

    static{
        for(var opcode: OpCode.values()) {
            var record = opcode.frameClass;
            if (record == null || !record.isRecord())
                throw new IllegalArgumentException("OpCode " + opcode + " is not a record");
            var fields = record.getRecordComponents();
            var components = Arrays.stream(fields).map(RecordComponent::getType).toArray(Class<?>[]::new);
            opCodeToConstructors.put(opcode, new PacketComponents(record, components));
        }
    }

    public Reader.ProcessStatus process(ByteBuffer buffer, OpCode opcode) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        lock.lock();
        try {
            if (state == State.DONE)
                throw new IllegalStateException("Reader already done without reset");
            if (state == State.ERROR)
                throw new IllegalStateException("Reader already in error state");
            var packet = opCodeToConstructors.get(opcode);
            for (var i = currentPosition; i < packet.components.length; i++) {
                var component = packet.components[i];
                var reader = packetToReader.get(component);
                var status = reader.process(buffer);
                if (status == Reader.ProcessStatus.DONE) {
                    currentValues.add(reader.get());
                    reader.reset();
                    currentPosition = 0;
                } else if (status == Reader.ProcessStatus.REFILL) {
                    currentPosition = i;
                    return Reader.ProcessStatus.REFILL;
                } else if (status == Reader.ProcessStatus.ERROR) {
                    state = State.ERROR;
                    return Reader.ProcessStatus.ERROR;
                }
            }
            state = State.DONE;
            value = packet.packet.getDeclaredConstructor(packet.components).newInstance(currentValues.toArray());
            return Reader.ProcessStatus.DONE;
        }finally {
            lock.unlock();
        }
    }

    public Frame get() {
        lock.lock();
        try {
            if (state != State.DONE)
                throw new IllegalStateException("Reader not done");
            return value;
        }finally {
            lock.unlock();
        }
    }

    public void reset(){
        lock.lock();
        try {
            for (var reader : packetToReader.values()) {
                reader.reset();
            }
            state = State.WAITING_PAYLOAD;
            currentPosition = 0;
            currentValues.clear();
        }finally {
            lock.unlock();
        }
    }

}
