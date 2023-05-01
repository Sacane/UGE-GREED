package fr.ramatellier.greed.server.packet.frame;

import fr.ramatellier.greed.server.packet.component.IDComponent;
import fr.ramatellier.greed.server.util.FrameKind;

public sealed interface TransferFrame extends Frame permits WorkAssignmentPacket, WorkRequestPacket, WorkRequestResponsePacket, WorkResponsePacket {
    IDComponent src();
    IDComponent dst();

    @Override
    default FrameKind kind(){
        return FrameKind.TRANSFER;
    }
}
