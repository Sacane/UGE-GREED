package fr.ramatellier.greed.server.packet;

public interface FullPacket extends Packet{
    void accept(PacketVisitor visitor);
}
