package fr.ramatellier.greed.server.packet.frame;

import fr.ramatellier.greed.server.packet.component.IDComponent;
import fr.ramatellier.greed.server.packet.component.RangePacket;
import fr.ramatellier.greed.server.util.OpCodes;

public record WorkAssignmentPacket(IDComponent src, IDComponent dst, long requestId, RangePacket range) implements TransferFrame {

    @Override
    public OpCodes opCode() {
        return OpCodes.WORK_ASSIGNMENT;
    }

}
