package fr.ramatellier.greed.server.reader;

import fr.ramatellier.greed.server.frame.component.*;
import fr.ramatellier.greed.server.frame.component.primitive.ByteComponent;
import fr.ramatellier.greed.server.frame.component.primitive.IntComponent;
import fr.ramatellier.greed.server.frame.component.primitive.LongComponent;
import fr.ramatellier.greed.server.frame.model.Frame;
import fr.ramatellier.greed.server.reader.component.*;
import fr.ramatellier.greed.server.frame.OpCode;
import fr.ramatellier.greed.server.reader.component.primitive.ByteComponentReader;
import fr.ramatellier.greed.server.reader.component.primitive.IntComponentReader;
import fr.ramatellier.greed.server.reader.component.primitive.LongComponentReader;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Thread-safe class used to decode any kind of {@link Frame} from a ByteBuffer.
 * It uses the {@link OpCode} to know which Packet to create.
 */
public class FrameReaderDecoder {
    private record PacketComponents(Class<? extends Frame> packet, Class<?>[] components){}
    private static final Map<OpCode, PacketComponents> opCodeToConstructors = new HashMap<>();
    private static final Map<Class<? extends GreedComponent>, Reader<? extends GreedComponent>> packetToReader = initPacketReader();
    private final ReentrantLock lock = new ReentrantLock();
    private Frame value;
    private int currentPosition;
    private final ArrayList<Object> currentReadingComponents = new ArrayList<>();
    private enum State{
        DONE, WAITING_PAYLOAD, ERROR
    }
    private State state = State.WAITING_PAYLOAD;
    private static Map<Class<? extends GreedComponent>, Reader<? extends GreedComponent>> initPacketReader(){
        var packetToReader = new HashMap<Class<? extends GreedComponent>, Reader<? extends GreedComponent>>();
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
        return packetToReader;
    }

    static{
        for(var opcode: OpCode.values()) {
            var record = opcode.frameClass;
            if (record == null || !record.isRecord())
                throw new IllegalArgumentException("OpCode " + opcode + " is not a record");
            var fields = record.getRecordComponents();
            var components = Arrays.stream(fields).map(FrameReaderDecoder::ensureAndGet).toArray(Class<?>[]::new);
            opCodeToConstructors.put(opcode, new PacketComponents(record, components));
        }
    }
    private static Class<?> ensureAndGet(RecordComponent component){
        Objects.requireNonNull(component);
        if(!GreedComponent.class.isAssignableFrom(component.getType())){
            throw new IllegalArgumentException("Record component " + component + " is not a greed component");
        }
        return component.getType();
    }

    /**
     * Process the given buffer to create a {@link Frame}.
     * @param buffer the buffer to process
     * @param opcode the opcode of the frame to create
     * @return the status of the process
     */
    public Reader.ProcessStatus process(ByteBuffer buffer, OpCode opcode) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Objects.requireNonNull(buffer);
        Objects.requireNonNull(opcode);
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
                    currentReadingComponents.add(reader.get());
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
            value = packet.packet.getDeclaredConstructor(packet.components)
                    .newInstance(currentReadingComponents.toArray());
            return Reader.ProcessStatus.DONE;
        }finally {
            lock.unlock();
        }
    }

    /**
     * Get the value of the frame reader.
     * @return the value of the reader
     */
    public Frame get() {
        lock.lock();
        try {
            if (state != State.DONE) throw new IllegalStateException("Reader not done");
            return value;
        }finally {
            lock.unlock();
        }
    }

    /**
     * Reset the reader to its initial state.
     */
    public void reset(){
        lock.lock();
        try {
            for (var reader : packetToReader.values()) {
                reader.reset();
            }
            state = State.WAITING_PAYLOAD;
            currentPosition = 0;
            currentReadingComponents.clear();
        }finally {
            lock.unlock();
        }
    }

}
