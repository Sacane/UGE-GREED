package fr.ramatellier.greed.server.packet;

import fr.ramatellier.greed.server.util.TramKind;

public interface FullPacket extends Packet{
    void accept(PacketVisitor visitor);
    TramKind kind();
    byte opCode();
}
