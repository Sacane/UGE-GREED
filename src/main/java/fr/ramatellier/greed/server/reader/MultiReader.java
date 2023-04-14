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

public class MultiReader{
    private record PacketComponents(Class<? extends FullPacket> packet, Class<?>[] components){}

    private static final Map<OpCodes, PacketComponents> opCodeToConstructors = new HashMap<>();
    private final Map<Class<?>, Reader<?>> packetToReader = initPacketReader();
    private FullPacket value;
    private int index;
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

        parsePacketAndInitialize();

//        opcodeToPacket.put(OpCodes.ADD_NODE, AddNodePacket.class);
//        opcodeToPacket.put(OpCodes.CONNECT, ConnectPacket.class);
//        opcodeToPacket.put(OpCodes.OK, ConnectOKPacket.class);
//        opcodeToPacket.put(OpCodes.KO, ConnectKOPacket.class);
//        opcodeToPacket.put(OpCodes.LOGOUT_DENIED, LogoutDeniedPacket.class);
//        opcodeToPacket.put(OpCodes.LOGOUT_GRANTED, LogoutGrantedPacket.class);
//        opcodeToPacket.put(OpCodes.DISCONNECTED, DisconnectedPacket.class);
//        opcodeToPacket.put(OpCodes.WORK, WorkRequestPacket.class);
//        opcodeToPacket.put(OpCodes.WORK_ASSIGNMENT, WorkAssignmentPacket.class);
//        opcodeToPacket.put(OpCodes.WORK_RESPONSE, WorkResponsePacket.class);
//        opcodeToPacket.put(OpCodes.WORK_REQUEST_RESPONSE, WorkRequestResponsePacket.class);
//        opcodeToPacket.put(OpCodes.PLEASE_RECONNECT, PleaseReconnectPacket.class);
//        opcodeToPacket.put(OpCodes.RECONNECT, ReconnectPacket.class);
//        opcodeToPacket.put(OpCodes.LOGOUT_REQUEST, LogoutRequestPacket.class);
    }

    private static void parsePacketAndInitialize(){
//        for(var value: OpCodes.values()){
//            switch(value){
//                case ADD_NODE, DISCONNECTED -> opCodeToConstructors.put(value, new Class<?>[]{IDPacket.class, IDPacket.class});
//                case OK, LOGOUT_REQUEST -> opCodeToConstructors.put(value, new Class<?>[]{IDPacket.class, IDPacketList.class});
//                case KO, LOGOUT_DENIED, LOGOUT_GRANTED -> {}
//                case CONNECT -> opCodeToConstructors.put(value, new Class<?>[]{IDPacket.class});
//                case WORK -> opCodeToConstructors.put(value, new Class<?>[]{IDPacket.class, IDPacket.class, Long.class, CheckerPacket.class, RangePacket.class, Long.class});
//                case WORK_ASSIGNMENT -> opCodeToConstructors.put(value, new Class<?>[]{IDPacket.class, IDPacket.class, Long.class, CheckerPacket.class, RangePacket.class, Long.class});
//                case WORK_RESPONSE -> opCodeToConstructors.put(value, new Class<?>[]{IDPacket.class, IDPacket.class, Long.class, ResponsePacket.class});
//                case WORK_REQUEST_RESPONSE -> opCodeToConstructors.put(value, new Class<?>[]{IDPacket.class, IDPacket.class, Long.class, ResponsePacket.class});
//                case PLEASE_RECONNECT -> opCodeToConstructors.put(value, new Class<?>[]{IDPacket.class, IDPacket.class});
//                case RECONNECT -> opCodeToConstructors.put(value, new Class<?>[]{IDPacket.class, IDPacket.class});
//            }
//        }
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
        System.out.println("Processing opcode " + opcode + " with state " + state);
        if(state == State.DONE)
            throw new IllegalStateException("Reader already done without reset");
        if(state == State.ERROR)
            throw new IllegalStateException("Reader already in error state");

        var packet = opCodeToConstructors.get(opcode);
//        var values = new ArrayList<>(packet.components.length);
        System.out.println("Packet: " + packet.packet);
        for(var i = index; i < packet.components.length; i++){
            var component = packet.components[i];
            System.out.println("Component: " + component);
            var reader = packetToReader.get(component);
            var status = reader.process(buffer);
            System.out.println("Status: " + status);
            if(status == Reader.ProcessStatus.DONE){
                currentValues.add(reader.get());
                reader.reset();
                System.out.println("READER DONE");
                index = 0;
            } else if (status == Reader.ProcessStatus.REFILL) {
                index = i;
                System.out.println("State: " + state);
                return Reader.ProcessStatus.REFILL;
            } else if(status == Reader.ProcessStatus.ERROR){
                System.out.println("ERROR");
                state = State.ERROR;
                return Reader.ProcessStatus.ERROR;
            }
        }
        state = State.DONE;
        value = packet.packet.getDeclaredConstructor(packet.components).newInstance(currentValues.toArray());
        System.out.println("DONE");
        return Reader.ProcessStatus.DONE;
    }

    public FullPacket get() {
        if(state != State.DONE)
            throw new IllegalStateException("Reader not done");
        return value;
    }

    public void reset(){
//        for(var reader: readers){
//            reader.reset();
//        }
        for(var reader: packetToReader.values()){
            reader.reset();
        }
        state = State.WAITING_PAYLOAD;
        index = 0;
        currentValues.clear();
    }

}
