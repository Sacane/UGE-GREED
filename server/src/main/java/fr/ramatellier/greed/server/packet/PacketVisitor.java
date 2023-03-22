package fr.ramatellier.greed.server.packet;

public interface PacketVisitor {
    void visit(ConnectPacket packet);
    void visit(ConnectOKPacket packet);
    void visit(ConnectKOPacket packet);
    void visit(AddNodePacket packet);
    void visit(WorkRequestPacket packet);
    default void visit(FullPacket packet) {
        switch(packet){
            case ConnectPacket p -> visit(p);
            case ConnectOKPacket p -> visit(p);
            case ConnectKOPacket p -> visit(p);
            case AddNodePacket p -> visit(p);
            case WorkRequestPacket p -> visit(p);
        }
    }
}
