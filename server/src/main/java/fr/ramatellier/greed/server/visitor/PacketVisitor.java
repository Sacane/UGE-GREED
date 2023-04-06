package fr.ramatellier.greed.server.visitor;

import fr.ramatellier.greed.server.packet.full.*;

public interface PacketVisitor {
    void visit(ConnectPacket packet);
    void visit(ConnectOKPacket packet);
    void visit(ConnectKOPacket packet);
    void visit(AddNodePacket packet);
    void visit(WorkRequestPacket packet);
    void visit(WorkAssignmentPacket packet);
    void visit(WorkResponsePacket packet);
    void visit(WorkRequestResponsePacket packet);
    void visit(LogoutRequestPacket packet);
    void visit(LogoutDeniedPacket packet);
    void visit(LogoutGrantedPacket packet);
    void visit(PleaseReconnectPacket packet);
    void visit(ReconnectPacket packet);
    void visit(DisconnectedPacket packet);

    default void visit(FullPacket packet) {
        switch(packet) {
            case ConnectPacket p -> visit(p);
            case ConnectOKPacket p -> visit(p);
            case ConnectKOPacket p -> visit(p);
            case AddNodePacket p -> visit(p);
            case WorkRequestPacket p -> visit(p);
            case WorkAssignmentPacket p -> visit(p);
            case WorkResponsePacket p -> visit(p);
            case WorkRequestResponsePacket p -> visit(p);
            case LogoutRequestPacket p -> visit(p);
            case LogoutDeniedPacket p -> visit(p);
            case LogoutGrantedPacket p -> visit(p);
            case PleaseReconnectPacket p -> visit(p);
            case ReconnectPacket p -> visit(p);
            case DisconnectedPacket p -> visit(p);
            case BroadcastPacket p -> visit(p);
            case LocalPacket p -> visit(p);
            case TransferPacket p -> visit(p);
        }
    }
}
