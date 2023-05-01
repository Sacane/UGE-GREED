package fr.ramatellier.greed.server.packet.full;

import fr.ramatellier.greed.server.util.OpCodes;
import fr.ramatellier.greed.server.visitor.PacketVisitor;
import fr.ramatellier.greed.server.packet.*;
import fr.ramatellier.greed.server.util.TramKind;

import java.nio.ByteBuffer;

public sealed interface FullPacket extends GreedComponent permits BroadcastPacket, LocalPacket, TransferPacket {
    default void accept(PacketVisitor visitor){
        visitor.visit(this);
    }

    /**
     * The kind of the packet (broadcast, local, transfer)
     * @return the kind of the packet
     */
    TramKind kind();

    /**
     * The {@link OpCodes} of the packet
     * @return the opcode of the packet
     */
    OpCodes opCode();

    /**
     * Put the packet body into the buffer
     * @param buffer the buffer that receive the packet
     */
    void put(ByteBuffer buffer);
    default void putInBuffer(ByteBuffer buffer) {
        buffer.put(kind().BYTES);
        buffer.put(opCode().BYTES);
        put(buffer);
    }
}
