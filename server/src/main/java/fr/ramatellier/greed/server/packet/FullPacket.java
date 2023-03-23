package fr.ramatellier.greed.server.packet;

import fr.ramatellier.greed.server.util.TramKind;

public sealed interface FullPacket extends Packet permits AddNodePacket, ConnectKOPacket, ConnectOKPacket, ConnectPacket, WorkRequestPacket, WorkResponsePacket {
    default void accept(PacketVisitor visitor){
        visitor.visit(this);
    }
    TramKind kind();
    byte opCode();
}
