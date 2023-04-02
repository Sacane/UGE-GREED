package fr.ramatellier.greed.server.packet.full;

import fr.ramatellier.greed.server.packet.sub.IDPacket;
import fr.ramatellier.greed.server.util.TramKind;

public sealed interface BroadcastPacket extends FullPacket permits AddNodePacket, BroadcastOnePacket, DisconnectedPacket {
    @Override
    default TramKind kind(){
        return TramKind.BROADCAST;
    }
    IDPacket src();
}
