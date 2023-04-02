package fr.ramatellier.greed.server.packet.full;

import fr.ramatellier.greed.server.util.OpCodes;
import fr.ramatellier.greed.server.visitor.PacketVisitor;
import fr.ramatellier.greed.server.Server;
import fr.ramatellier.greed.server.packet.*;
import fr.ramatellier.greed.server.util.TramKind;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public sealed interface FullPacket extends Packet permits AddNodePacket, BroadcastPacket, ConnectKOPacket, ConnectOKPacket, ConnectPacket, DisconnectedPacket, LocalPacket, LogoutDeniedPacket, LogoutGrantedPacket, LogoutRequestPacket, PleaseReconnectPacket, ReconnectPacket, TransferPacket, WorkAssignmentPacket, WorkRequestPacket, WorkRequestResponsePacket, WorkResponsePacket {
    default void accept(PacketVisitor visitor){
        visitor.visit(this);
    }
    TramKind kind();
    OpCodes opCode();

    default boolean onConditionTransfer(boolean condition, InetSocketAddress to, Server server){
        if(kind() == TramKind.TRANSFER && condition){
            server.transfer(to, this);
            return true;
        }
        return false;
    }

    default void putHeader(ByteBuffer buffer) {
        buffer.put(kind().BYTES);
        buffer.put(opCode().BYTES);
    }

}
