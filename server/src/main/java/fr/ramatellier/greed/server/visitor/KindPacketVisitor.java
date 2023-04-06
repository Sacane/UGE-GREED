package fr.ramatellier.greed.server.visitor;

import fr.ramatellier.greed.server.packet.full.*;

public class KindPacketVisitor implements PacketVisitor{
    @Override
    public void visit(ConnectPacket packet) {

    }

    @Override
    public void visit(ConnectOKPacket packet) {

    }

    @Override
    public void visit(ConnectKOPacket packet) {

    }

    @Override
    public void visit(AddNodePacket packet) {

    }

    @Override
    public void visit(WorkRequestPacket packet) {

    }

    @Override
    public void visit(WorkAssignmentPacket packet) {

    }

    @Override
    public void visit(WorkResponsePacket packet) {

    }

    @Override
    public void visit(WorkRequestResponsePacket packet) {

    }

    @Override
    public void visit(LogoutRequestPacket packet) {

    }

    @Override
    public void visit(LogoutDeniedPacket packet) {

    }

    @Override
    public void visit(LogoutGrantedPacket packet) {

    }

    @Override
    public void visit(PleaseReconnectPacket packet) {

    }

    @Override
    public void visit(ReconnectPacket packet) {

    }

    @Override
    public void visit(DisconnectedPacket packet) {

    }
}
