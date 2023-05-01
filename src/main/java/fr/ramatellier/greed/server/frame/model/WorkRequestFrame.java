package fr.ramatellier.greed.server.frame.model;

import fr.ramatellier.greed.server.frame.component.RangeComponent;
import fr.ramatellier.greed.server.frame.component.CheckerComponent;
import fr.ramatellier.greed.server.frame.component.IDComponent;
import fr.ramatellier.greed.server.util.OpCodes;

public record WorkRequestFrame(IDComponent src, IDComponent dst, Long requestId, CheckerComponent checker, RangeComponent range, long max) implements TransferFrame {

    @Override
    public OpCodes opCode() {
        return OpCodes.WORK;
    }
}
