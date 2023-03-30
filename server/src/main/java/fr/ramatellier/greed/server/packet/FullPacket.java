package fr.ramatellier.greed.server.packet;

import fr.ramatellier.greed.server.Server;
import fr.ramatellier.greed.server.util.TramKind;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public sealed interface FullPacket extends Packet permits AddNodePacket, ConnectKOPacket, ConnectOKPacket, ConnectPacket, LogoutDeniedPacket, LogoutGrantedPacket, LogoutRequestPacket, PleaseReconnectPacket, ReconnectPacket, WorkAssignmentPacket, WorkRequestPacket, WorkRequestResponsePacket, WorkResponsePacket {
    default void accept(PacketVisitor visitor){
        visitor.visit(this);
    }
    TramKind kind();
    byte opCode();

    default boolean onConditionTransfer(boolean condition, InetSocketAddress to, Server server){
        if(kind() == TramKind.TRANSFERT && condition){
            server.transfer(to, this);
            return true;
        }
        return false;
    }

    default void putHeader(ByteBuffer buffer) {
        buffer.put(kind().BYTES);
        buffer.put(opCode());
    }
}
