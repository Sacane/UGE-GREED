package fr.ramatellier.greed.server.packet.frame;

import fr.ramatellier.greed.server.packet.component.RangePacket;
import fr.ramatellier.greed.server.packet.component.CheckerComponent;
import fr.ramatellier.greed.server.packet.component.IDComponent;
import fr.ramatellier.greed.server.util.OpCodes;

public record WorkRequestPacket(IDComponent src, IDComponent dst, Long requestId, CheckerComponent checker, RangePacket range, long max) implements TransferFrame {

    @Override
    public OpCodes opCode() {
        return OpCodes.WORK;
    }
}
