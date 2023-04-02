package fr.ramatellier.greed.server.packet.full;

import fr.ramatellier.greed.server.packet.sub.IDPacket;
import fr.ramatellier.greed.server.util.TramKind;
import fr.ramatellier.greed.server.visitor.PacketVisitor;

public sealed interface BroadcastPacket extends FullPacket permits AddNodePacket, BroadcastOnePacket, DisconnectedPacket {
    default void accept(PacketVisitor visitor){
        visitor.visit(this);
    }
    @Override
    default TramKind kind(){
        return TramKind.BROADCAST;
    }
    IDPacket src();
}
