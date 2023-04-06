package fr.ramatellier.greed.server.packet.full;

import fr.ramatellier.greed.server.util.OpCodes;
import fr.ramatellier.greed.server.visitor.PacketVisitor;
import fr.ramatellier.greed.server.packet.*;
import fr.ramatellier.greed.server.util.TramKind;

import java.nio.ByteBuffer;

public sealed interface FullPacket extends Packet permits BroadcastPacket, LocalPacket, TransferPacket {
    default void accept(PacketVisitor visitor){
        visitor.visit(this);
    }
    TramKind kind();
    OpCodes opCode();
    void put(ByteBuffer buffer);
    default void putHeader(ByteBuffer buffer) {
        buffer.put(kind().BYTES);
        buffer.put(opCode().BYTES);
    }
    default void putInBuffer(ByteBuffer buffer) {
        putHeader(buffer);
        put(buffer);
    }


}
