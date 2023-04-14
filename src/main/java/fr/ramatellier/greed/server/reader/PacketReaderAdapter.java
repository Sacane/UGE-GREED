package fr.ramatellier.greed.server.reader;

import fr.ramatellier.greed.server.packet.full.*;
import fr.ramatellier.greed.server.packet.sub.*;
import fr.ramatellier.greed.server.reader.primitive.ByteReader;
import fr.ramatellier.greed.server.reader.primitive.IntReader;
import fr.ramatellier.greed.server.reader.primitive.LongReader;
import fr.ramatellier.greed.server.reader.sub.*;
import fr.ramatellier.greed.server.util.OpCodes;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class PacketReaderAdapter {
    private record PacketComponents(Class<? extends FullPacket> packet, Class<?>[] components){}
    private static final Map<OpCodes, PacketComponents> opCodeToConstructors = new HashMap<>();
    private final Map<Class<?>, Reader<?>> packetToReader = initPacketReader();
    private FullPacket value;
    private int currentPosition;
    private final ArrayList<Object> currentValues = new ArrayList<>();
    private enum State{
        DONE, WAITING_PAYLOAD, ERROR
    }
    private State state = State.WAITING_PAYLOAD;
    private Map<Class<?>, Reader<?>> initPacketReader(){
        var packetToReader = new HashMap<Class<?>, Reader<?>>();
        packetToReader.put(StringPacket.class, new StringReader());
        packetToReader.put(IDPacket.class, new IDReader());
        packetToReader.put(IpAddressPacket.class, new IPReader());
        packetToReader.put(CheckerPacket.class, new CheckerPacketReader());
        packetToReader.put(RangePacket.class, new RangePacketReader());
        packetToReader.put(DestinationPacket.class, new DestinationPacketReader());
        packetToReader.put(ResponsePacket.class, new ResponsePacketReader());
        packetToReader.put(IDPacketList.class, new IDListReader());
        packetToReader.put(Long.class, new LongReader());
        packetToReader.put(Integer.class, new IntReader());
        packetToReader.put(Byte.class, new ByteReader());
        return packetToReader;
    }

    static{
        for(var opcode: OpCodes.values()) {
            var record = packetFromOpCode(opcode);
            if (record == null || !record.isRecord())
                throw new IllegalArgumentException("OpCode " + opcode + " is not a record");
            var fields = record.getRecordComponents();
            var components = Arrays.stream(fields).map(RecordComponent::getType).toArray(Class<?>[]::new);
            opCodeToConstructors.put(opcode, new PacketComponents(record, components));
        }
    }

    private static Class<? extends FullPacket> packetFromOpCode(OpCodes opcode){
        return switch(opcode){
            case ADD_NODE -> AddNodePacket.class;
            case CONNECT -> ConnectPacket.class;
            case OK -> ConnectOKPacket.class;
            case KO -> ConnectKOPacket.class;
            case LOGOUT_DENIED -> LogoutDeniedPacket.class;
            case LOGOUT_GRANTED -> LogoutGrantedPacket.class;
            case DISCONNECTED -> DisconnectedPacket.class;
            case WORK -> WorkRequestPacket.class;
            case WORK_ASSIGNMENT -> WorkAssignmentPacket.class;
            case WORK_RESPONSE -> WorkResponsePacket.class;
            case WORK_REQUEST_RESPONSE -> WorkRequestResponsePacket.class;
            case PLEASE_RECONNECT -> PleaseReconnectPacket.class;
            case RECONNECT -> ReconnectPacket.class;
            case LOGOUT_REQUEST -> LogoutRequestPacket.class;
        };
    }

    public Reader.ProcessStatus process(ByteBuffer buffer, OpCodes opcode) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        if(state == State.DONE)
            throw new IllegalStateException("Reader already done without reset");
        if(state == State.ERROR)
            throw new IllegalStateException("Reader already in error state");
        var packet = opCodeToConstructors.get(opcode);
        for(var i = currentPosition; i < packet.components.length; i++){
            var component = packet.components[i];
            var reader = packetToReader.get(component);
            var status = reader.process(buffer);
            if(status == Reader.ProcessStatus.DONE){
                currentValues.add(reader.get());
                reader.reset();
                currentPosition = 0;
            } else if (status == Reader.ProcessStatus.REFILL) {
                currentPosition = i;
                return Reader.ProcessStatus.REFILL;
            } else if(status == Reader.ProcessStatus.ERROR){
                state = State.ERROR;
                return Reader.ProcessStatus.ERROR;
            }
        }
        state = State.DONE;
        value = packet.packet.getDeclaredConstructor(packet.components).newInstance(currentValues.toArray());
        return Reader.ProcessStatus.DONE;
    }

    public FullPacket get() {
        if(state != State.DONE)
            throw new IllegalStateException("Reader not done");
        return value;
    }

    public void reset(){
        for(var reader: packetToReader.values()){
            reader.reset();
        }
        state = State.WAITING_PAYLOAD;
        currentPosition = 0;
        currentValues.clear();
    }

}
