package fr.ramatellier.greed.server.packet.full;

import fr.ramatellier.greed.server.packet.sub.IDPacket;
import fr.ramatellier.greed.server.util.TramKind;

public sealed interface TransferPacket extends FullPacket permits TransferPacketOne, WorkAssignmentPacket, WorkRequestPacket, WorkRequestResponsePacket, WorkResponsePacket {
    IDPacket src();
    IDPacket dst();

    @Override
    default TramKind kind(){
        return TramKind.TRANSFER;
    }
}
