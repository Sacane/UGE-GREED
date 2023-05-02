package fr.ramatellier.greed.server.frame.model;

import fr.ramatellier.greed.server.util.OpCodes;
import fr.ramatellier.greed.server.visitor.PacketVisitor;
import fr.ramatellier.greed.server.frame.FrameKind;

/**
 * A Frame is a packet that can be sent over the network into a byteBuffer.
 * It has a {@link FrameKind} and an {@link OpCodes} that are used to identify the packet and its global use.
 */
public sealed interface Frame permits BroadcastFrame, LocalFrame, TransferFrame {
    default void accept(PacketVisitor visitor){
        visitor.visit(this);
    }

    /**
     * The kind of the packet (broadcast, local, transfer)
     * @return the kind of the packet
     */
    FrameKind kind();
}
