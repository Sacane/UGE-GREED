package fr.ramatellier.greed.server.model.frame;

import fr.ramatellier.greed.server.model.component.RangeComponent;
import fr.ramatellier.greed.server.model.component.CheckerComponent;
import fr.ramatellier.greed.server.model.component.IDComponent;
import fr.ramatellier.greed.server.util.OpCodes;

public record WorkRequestFrame(IDComponent src, IDComponent dst, Long requestId, CheckerComponent checker, RangeComponent range, long max) implements TransferFrame {

    @Override
    public OpCodes opCode() {
        return OpCodes.WORK;
    }
}
