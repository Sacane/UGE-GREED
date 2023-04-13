package fr.ramatellier.greed.server.reader;

import fr.ramatellier.greed.server.packet.full.*;
import fr.ramatellier.greed.server.packet.sub.*;
import fr.ramatellier.greed.server.util.OpCodes;

import java.nio.ByteBuffer;
import java.util.List;

public class MultiReader{
    private final Reader<Part>[] readers;
    private final OpCodes opcode;

    @SafeVarargs
    public MultiReader(OpCodes opcode, Reader<Part>... readers) {
        this.opcode = opcode;
        this.readers = readers;
    }

    public Reader.ProcessStatus process(ByteBuffer buffer){
        for(var reader: readers){
            var status = reader.process(buffer);
            switch (status) {
                case DONE -> {}
                case REFILL -> {
                    return Reader.ProcessStatus.REFILL;
                }
                case ERROR -> {
                    return Reader.ProcessStatus.ERROR;
                }
            }
        }
        return Reader.ProcessStatus.DONE;
    }

    public FullPacket get(){
        return switch(opcode){
            case ADD_NODE -> new AddNodePacket((IDPacket) readers[0].get(), (IDPacket) readers[1].get());
            case OK -> new ConnectOKPacket((IDPacket) readers[0].get(), (IDPacketList) readers[1].get());
            case KO -> new ConnectKOPacket();
            case CONNECT -> new ConnectPacket((IDPacket) readers[0].get());
            case DISCONNECTED -> new DisconnectedPacket((IDPacket) readers[0].get(), (IDPacket) readers[1].get());
            case LOGOUT_DENIED -> new LogoutDeniedPacket();
            case WORK -> new WorkRequestPacket(
                    (IDPacket) readers[0].get(),
                    (IDPacket) readers[1].get(),
                    (LongPacketPart) readers[2].get(),
                    (CheckerPacket) readers[3].get(),
                    (RangePacket) readers[4].get(),
                    (LongPacketPart) readers[5].get()
            );
            case WORK_ASSIGNMENT -> new WorkAssignmentPacket(
                    (IDPacket) readers[0].get(),
                    (IDPacket) readers[1].get(),
                    (LongPacketPart) readers[2].get(),
                    (RangePacket) readers[4].get()
            );
            case WORK_REQUEST_RESPONSE -> new WorkRequestResponsePacket(
                    (IDPacket) readers[0].get(),
                    (IDPacket) readers[1].get(),
                    (LongPacketPart) readers[2].get(),
                    (LongPacketPart) readers[3].get()
            );
            case WORK_RESPONSE -> new WorkResponsePacket(
                    (IDPacket) readers[0].get(),
                    (IDPacket) readers[1].get(),
                    (LongPacketPart) readers[2].get(),
                    (ResponsePacket) readers[3].get()
            );
            case PLEASE_RECONNECT -> new PleaseReconnectPacket((IDPacket) readers[0].get());
            case RECONNECT -> new ReconnectPacket((IDPacket) readers[0].get(), (IDPacketList) readers[1].get());
            case LOGOUT_REQUEST -> new LogoutRequestPacket((IDPacket) readers[0].get(), (IDPacketList) readers[1].get());
            case LOGOUT_GRANTED -> new LogoutGrantedPacket();
        };
    }

    public void reset(){
        for(var reader: readers){
            reader.reset();
        }
    }
}
