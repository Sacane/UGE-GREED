package fr.ramatellier.greed.server.packet;

public interface PacketVisitor {
    void visit(ConnectPacket packet);
    void visit(ConnectOKPacket packet);
    void visit(ConnectKOPacket packet);
}
